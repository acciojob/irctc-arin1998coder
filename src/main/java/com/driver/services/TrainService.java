package com.driver.services;

import com.driver.EntryDto.AddTrainEntryDto;
import com.driver.EntryDto.SeatAvailabilityEntryDto;
import com.driver.model.Passenger;
import com.driver.model.Station;
import com.driver.model.Ticket;
import com.driver.model.Train;
import com.driver.repository.TrainRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
public class TrainService {

    @Autowired
    TrainRepository trainRepository;

    public Integer addTrain(AddTrainEntryDto trainEntryDto){

        //Add the train to the trainRepository
        //and route String logic to be taken from the Problem statement.
        //Save the train and return the trainId that is generated from the database.
        //Avoid using the lombok library
        StringBuilder routeBuilder = new StringBuilder();
        String route;
        LocalTime departureTime;
        int noOfSeats;
        int size = trainEntryDto.getStationRoute().size();
        int i=0;
        for(Station station:trainEntryDto.getStationRoute()){
            if(i==size-1)
                routeBuilder.append(station.name());
            else
                routeBuilder.append(station.name()+",");
            i++;
        }

        route = routeBuilder.toString();
        departureTime = trainEntryDto.getDepartureTime();
        noOfSeats = trainEntryDto.getNoOfSeats();

        Train train = new Train();
        train.setRoute(route);
        train.setDepartureTime(departureTime);
        train.setNoOfSeats(noOfSeats);

        trainRepository.save(train);

        return train.getTrainId();
    }

    public Integer calculateAvailableSeats(SeatAvailabilityEntryDto seatAvailabilityEntryDto){

        //Calculate the total seats available
        //Suppose the route is A B C D
        //And there are 2 seats avaialble in total in the train
        //and 2 tickets are booked from A to C and B to D.
        //The seat is available only between A to C and A to B. If a seat is empty between 2 station it will be counted to our final ans
        //even if that seat is booked post the destStation or before the boardingStation
        //Inshort : a train has totalNo of seats and there are tickets from and to different locations
        //We need to find out the available seats between the given 2 stations.

        int trainId = seatAvailabilityEntryDto.getTrainId();
        Station src = seatAvailabilityEntryDto.getFromStation();
        Station des = seatAvailabilityEntryDto.getToStation();

        Train train = trainRepository.findById(trainId).get();

        int bookedSeats = 0;
        int seatsAvailable = train.getNoOfSeats();

        for(Ticket ticket:train.getBookedTickets()){
            bookedSeats+=ticket.getPassengersList().size();
        }

        int totalSeats =  seatsAvailable + bookedSeats ;

        String cities[] = train.getRoute().split(",");
        HashMap<String, Integer> cityMap = new HashMap<>();

        for (int i = 0; i < cities.length; i++) {
            cityMap.put(cities[i], i);
        }

        int srcLocation = cityMap.get(src.name());
        int desLocation = cityMap.get(des.name());
//        System.out.println(cityMap);

        int seatsbookedexcluding = 0;
        //find the num of booked seats from any source to des before the given source + any source to des after the given des
        for(Ticket ticket:train.getBookedTickets()){

            int tktSrcLocation = cityMap.get(ticket.getFromStation().name());
            int tktDesLocation = cityMap.get(ticket.getToStation().name());

            //num of passengers on a single tkt
            //so that many seats would be occupied
            int passengers = ticket.getPassengersList().size();

            if(tktDesLocation<=srcLocation)
                seatsbookedexcluding+=ticket.getPassengersList().size();
            else if(tktSrcLocation>=desLocation)
                seatsbookedexcluding+=ticket.getPassengersList().size();

        }

        int seatsBookedBWSRCTODES = bookedSeats - seatsbookedexcluding;

       return totalSeats - seatsBookedBWSRCTODES; //available seats b/w given src to des
    }

    public Integer calculatePeopleBoardingAtAStation(Integer trainId,Station station) throws Exception{

        //We need to find out the number of people who will be boarding a train from a particular station
        //if the trainId is not passing through that station
        //throw new Exception("Train is not passing from this station");
        //  in a happy case we need to find out the number of such people.

        Train train = trainRepository.findById(trainId).get();

        int num=0;

        String boardingStation = station.name();

        if(!train.getRoute().contains(boardingStation)){
            throw new Exception("Train is not passing from this station");
        }

        for(Ticket ticket :train.getBookedTickets()){

            if(ticket.getFromStation()==station){
                num+=ticket.getPassengersList().size();
            }
        }
        return num;
    }

    public Integer calculateOldestPersonTravelling(Integer trainId){

        //Throughout the journey of the train between any 2 stations
        //We need to find out the age of the oldest person that is travelling the train
        //If there are no people travelling in that train you can return 0

        Train train = trainRepository.findById(trainId).get();

        if(train.getBookedTickets().isEmpty()) return 0;

        int oldest = 0;

        for(Ticket ticket:train.getBookedTickets()){
            for(Passenger passenger: ticket.getPassengersList()){
                if(passenger.getAge()>oldest){
                    oldest=passenger.getAge();
                }
            }
        }

        return oldest;
    }

    public List<Integer> trainsBetweenAGivenTime(Station station, LocalTime startTime, LocalTime endTime){

        //When you are at a particular station you need to find out the number of trains that will pass through a given station
        //between a particular time frame both start time and end time included.
        //You can assume that the date change doesn't need to be done ie the travel will certainly happen with the same date (More details
        //in problem statement)
        //You can also assume the seconds and milli seconds value will be 0 in a LocalTime format.

        List<Train> trains = trainRepository.findAll();

        List<Integer> ans = new ArrayList<>();


        for(Train train:trains){
            String[] cities = train.getRoute().split(",");
            HashMap<String, Integer> routeMap = new HashMap<>();

            for (int i = 0; i < cities.length; i++) {
                routeMap.put(cities[i], i);
            }
            //if station is not part of route
            if(!routeMap.containsKey(station.name())){
                continue;
            }
            LocalTime depTime = train.getDepartureTime();
            LocalTime stationArrivalTime = depTime.plusHours(routeMap.get(station.name()));

            //check if stationArrivalTIme lies b/w start and end time
            if(stationArrivalTime.compareTo(startTime)>=0 && stationArrivalTime.compareTo(endTime)<=0){
                ans.add(train.getTrainId());
            }
        }

        return ans;
    }

}
