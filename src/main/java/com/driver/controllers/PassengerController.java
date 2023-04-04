package com.driver.controllers;


import com.driver.model.Passenger;
import com.driver.model.Ticket;
import com.driver.repository.PassengerRepository;
import com.driver.repository.TicketRepository;
import com.driver.services.PassengerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("passenger")
public class PassengerController {

    @Autowired
    PassengerService passengerService;

//    //addedby me
//    @Autowired
//    TicketRepository ticketRepository;

    @PostMapping("/create")
    public Integer registerPassenger(@RequestBody Passenger passenger){
        return passengerService.addPassenger(passenger);
    }

//    //added by me
//    @GetMapping("/passenger_count")
//    public int getPassengersOnATicket(@RequestParam int ticketId){
//        Ticket ticket = ticketRepository.findById(ticketId).get();
//        return ticket.getPassengersList().size();
//    }

}
