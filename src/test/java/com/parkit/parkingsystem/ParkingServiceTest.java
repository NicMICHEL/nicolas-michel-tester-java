package com.parkit.parkingsystem;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.FareCalculatorService;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ParkingServiceTest {

    private static ParkingService parkingService;

    @Mock
    private static InputReaderUtil inputReaderUtil;
    @Mock
    private static ParkingSpotDAO parkingSpotDAO;
    @Mock
    private static TicketDAO ticketDAO;
    @Mock
    private static FareCalculatorService fareCalculatorService;

    @BeforeEach
    private void setUpPerTest() {
        try {
            parkingService = new ParkingService(fareCalculatorService, inputReaderUtil, parkingSpotDAO, ticketDAO);
        } catch (Exception e) {
            e.printStackTrace();
            throw  new RuntimeException("Failed to set up test mock objects");
        }
    }

    @Test
    public void processExitingVehicleTest() throws Exception{
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);
        Ticket ticket = new Ticket();
        ticket.setInTime(new Date(System.currentTimeMillis() - (60*60*1000)));
        ticket.setParkingSpot(parkingSpot);
        ticket.setVehicleRegNumber("ABCDEF");
        when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(true);
        when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);
        when(ticketDAO.getTicket(anyString())).thenReturn(ticket);
        when(ticketDAO.getNbTicket("ABCDEF")).thenReturn(2);
        doNothing().when(fareCalculatorService).calculateFare(ticket, true);
        parkingService.processExitingVehicle();
        verify(parkingSpotDAO).updateParking(any(ParkingSpot.class));
        verify(fareCalculatorService).calculateFare(ticket, true);
        verify(ticketDAO).getNbTicket("ABCDEF");
        verify(ticketDAO).getTicket(anyString());
        verify(ticketDAO).updateTicket(any(Ticket.class));
    }

    @Test
    public void testProcessIncomingVehicle() {
        try {
            when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
        } catch (Exception e) {
            e.printStackTrace();
            throw  new RuntimeException("Failed to set up mock readVehicleRegistrationNumber()");
        }
        when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);
        when(ticketDAO.getNbTicket("ABCDEF")).thenReturn(2);
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)).thenReturn(1);
        parkingService.processIncomingVehicle();
        verify(ticketDAO).getNbTicket("ABCDEF");
        verify(parkingSpotDAO).updateParking(any(ParkingSpot.class));
        verify(inputReaderUtil).readSelection();
        verify(parkingSpotDAO).getNextAvailableSlot(ParkingType.CAR);
    }

    @Test
    public void processExitingVehicleTestUnableUpdate() {
        try {
            when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
        } catch (Exception e) {
            e.printStackTrace();
            throw  new RuntimeException("Failed to set up mock readVehicleRegistrationNumber()");
        }
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);
        Ticket ticket = new Ticket();
        ticket.setInTime(new Date(System.currentTimeMillis() - (60*60*1000)));
        ticket.setParkingSpot(parkingSpot);
        ticket.setVehicleRegNumber("ABCDEF");
        when(ticketDAO.getTicket(anyString())).thenReturn(ticket);
        when(ticketDAO.getNbTicket("ABCDEF")).thenReturn(2);
        doNothing().when(fareCalculatorService).calculateFare(ticket, true);
        when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(false);
        parkingService.processExitingVehicle();
        verify(fareCalculatorService).calculateFare(ticket, true);
        verify(ticketDAO).getNbTicket("ABCDEF");
        verify(parkingSpotDAO, Mockito.times(0)).updateParking(any(ParkingSpot.class));
        verify(ticketDAO).getTicket(anyString());
        verify(ticketDAO).updateTicket(any(Ticket.class));
    }

    @Test
    public void testGetNextParkingNumberIfAvailable() {
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)).thenReturn(1);
        parkingService.getNextParkingNumberIfAvailable();
        verify(inputReaderUtil).readSelection();
        verify(parkingSpotDAO).getNextAvailableSlot(ParkingType.CAR);
    }

    @Test
    public void testGetNextParkingNumberIfAvailableParkingNumberNotFound() {
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)).thenReturn(-1);
        parkingService.getNextParkingNumberIfAvailable();
        verify(inputReaderUtil).readSelection();
        verify(parkingSpotDAO).getNextAvailableSlot(ParkingType.CAR);
    }

    @Test
    public void testGetNextParkingNumberIfAvailableParkingNumberWrongArgument() {
        when(inputReaderUtil.readSelection()).thenReturn(3);
        parkingService.getNextParkingNumberIfAvailable();
        verify(inputReaderUtil).readSelection();
    }
}
