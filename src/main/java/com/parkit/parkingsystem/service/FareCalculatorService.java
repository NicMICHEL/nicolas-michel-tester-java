package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;

public class FareCalculatorService {

    public void calculateFare(Ticket ticket, boolean discount){
        if( (ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime())) ){
            throw new IllegalArgumentException("Out time provided is incorrect:"+ticket.getOutTime().toString());
        }

        long inHour =  ticket.getInTime().getTime();
        long outHour = ticket.getOutTime().getTime();

        float duration = (float) (outHour - inHour) /(60*60*1000);
        if (duration<=0.5) duration = 0;

        int discountPercentage;
        if (discount) discountPercentage = 5;
        else discountPercentage = 0;

        switch (ticket.getParkingSpot().getParkingType()){
            case CAR: {
                ticket.setPrice(((double) (100 - discountPercentage) /100) * duration * Fare.CAR_RATE_PER_HOUR);
                break;
            }
            case BIKE: {
                ticket.setPrice(((double) (100 - discountPercentage) /100) * duration * Fare.BIKE_RATE_PER_HOUR);
                break;
            }
            default: throw new IllegalArgumentException("Unkown Parking Type");
        }
    }

    public void calculateFare(Ticket ticket){
        this.calculateFare(ticket, false);
    }

}
