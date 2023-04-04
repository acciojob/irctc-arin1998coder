package com.driver.services;


import com.driver.EntryDto.BookTicketEntryDto;
import com.driver.EntryDto.SeatAvailabilityEntryDto;
import com.driver.model.Passenger;
import com.driver.model.Station;
import com.driver.model.Ticket;
import com.driver.model.Train;
import com.driver.repository.PassengerRepository;
import com.driver.repository.TicketRepository;
import com.driver.repository.TrainRepository;
import io.swagger.models.auth.In;
import org.hibernate.mapping.Collection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

@Service
public class TicketService {

    @Autowired
    TicketRepository ticketRepository;

    @Autowired
    TrainRepository trainRepository;

    @Autowired
    PassengerRepository passengerRepository;

    @Autowired
    TrainService trainService;


    public Integer bookTicket(BookTicketEntryDto bookTicketEntryDto)throws Exception{

        //Check for validity
        //Use bookedTickets List from the TrainRepository to get bookings done against that train
        // Incase the there are insufficient tickets
        // throw new Exception("Less tickets are available");
        //otherwise book the ticket, calculate the price and other details
        //Save the information in corresponding DB Tables
        //Fare System : Check problem statement
        //Incase the train doesn't pass through the requested stations
        //throw new Exception("Invalid stations");
        //Save the bookedTickets in the train Object
        //Also in the passenger Entity change the attribute bookedTickets by using the attribute bookingPersonId.
       //And the end return the ticketId that has come from db

        HashMap<String,Integer> cityMap = new HashMap<>();

        List<Integer> passengers = bookTicketEntryDto.getPassengerIds();


        Integer bookingPersonId = bookTicketEntryDto.getBookingPersonId();

        Passenger bookingPerson = passengerRepository.findById(bookingPersonId).get();

        Integer trainId = bookTicketEntryDto.getTrainId();

        Station from = bookTicketEntryDto.getFromStation();

        Station to = bookTicketEntryDto.getToStation();

        int seatsRequired = bookTicketEntryDto.getNoOfSeats();

        Train train = trainRepository.findById(trainId).get();

        int bookedSeats = 0;

        for(Ticket ticket:train.getBookedTickets()){
            bookedSeats +=ticket.getPassengersList().size();
        }

        if(seatsRequired>train.getNoOfSeats() - bookedSeats){
            throw new Exception("Less tickets are available");
        }

        //store the trains route in the citymap
        String[] cities = train.getRoute().split(",");

        for (int i = 0; i < cities.length; i++) {
            cityMap.put(cities[i], i);
        }

        //check if the traiin passes through the from and to station
        if(!cityMap.containsKey(from.name()) || !cityMap.containsKey(to.name()) || cityMap.get(from.name())>cityMap.get(to.name())){

            throw new Exception("Invalid stations");
        }

        //calculate total fare
        int totalFare = (cityMap.get(to.name()) - cityMap.get(from.name())) * 300;

        Ticket ticket = new Ticket();

        List<Passenger> plist = new ArrayList<>();
        //make the passengerlist
//        int count =0;
        for(Integer pId : passengers){

            Passenger passenger1 = passengerRepository.findById(pId).get();
            plist.add(passenger1);
//            count++;
        }
//        System.out.println("Count "+count);

        //create Ticket

        ticket.setFromStation(from);
        ticket.setToStation(to);
        ticket.setTotalFare(totalFare);
        ticket.setTrain(train);
        ticket.setPassengersList(plist);

        //add ticket to trains bookedticketslist
        train.getBookedTickets().add(ticket);

        //add the ticket to the bookingperson's ticketlist
        if(bookingPerson.getBookedTickets().isEmpty()){
            bookingPerson.setBookedTickets(new ArrayList<>());
        }
        bookingPerson.getBookedTickets().add(ticket);

        //reduce the capacity of the train
        train.setNoOfSeats(train.getNoOfSeats() - seatsRequired);
//        trainRepository.save(train);
//        passengerRepository.save(bookingPerson);
        trainRepository.save(train);
        ticketRepository.save(ticket);

       return ticket.getTicketId();

    }
}
