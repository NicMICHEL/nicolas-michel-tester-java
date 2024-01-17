package com.parkit.parkingsystem.integration;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.FareCalculatorService;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;

import static junit.framework.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ParkingDataBaseIT {

    private static DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();
    private static ParkingSpotDAO parkingSpotDAO;
    private static TicketDAO ticketDAO;
    private static DataBasePrepareService dataBasePrepareService;
    private static FareCalculatorService fareCalculatorService;

    @Mock
    private static InputReaderUtil inputReaderUtil;

    @BeforeAll
    private static void setUp() throws Exception{
        parkingSpotDAO = new ParkingSpotDAO();
        parkingSpotDAO.dataBaseConfig = dataBaseTestConfig;
        ticketDAO = new TicketDAO();
        fareCalculatorService = new FareCalculatorService();
        ticketDAO.dataBaseConfig = dataBaseTestConfig;
        dataBasePrepareService = new DataBasePrepareService();
    }

    @BeforeEach
    private void setUpPerTest() throws Exception {
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
        dataBasePrepareService.clearDataBaseEntries();
    }

    @AfterAll
    private static void tearDown(){

    }

    @Test
    public void testParkingACar(){
        ParkingService parkingService = new ParkingService(fareCalculatorService, inputReaderUtil, parkingSpotDAO, ticketDAO);
        int initialNbTicket = ticketDAO.getNbTicket("ABCDEF");
        int initialAvailableSlot = parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR);
        parkingService.processIncomingVehicle();
        assertEquals((initialNbTicket + 1), ticketDAO.getNbTicket("ABCDEF") );
        assertEquals((initialAvailableSlot + 1), parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR) );
    }

    @Test
    public void testParkingLotExit(){
        testParkingACar();

        Ticket ticket = ticketDAO.getTicket("ABCDEF");
        double initialPrice = ticket.getPrice();
        Date initialOutTime = ticket.getOutTime();
        ticketDAO.addOneHourToTicket(ticket);

        ParkingService parkingService = new ParkingService(fareCalculatorService, inputReaderUtil, parkingSpotDAO, ticketDAO);
        parkingService.processExitingVehicle();

        //TODO: check that the fare generated and out time are populated correctly in the database
        ticket = ticketDAO.getTicket("ABCDEF");

        assertNull(initialOutTime);
        assertEquals(0, initialPrice);
        assertTrue(
                (   ((new Date().getTime()) - (ticket.getOutTime().getTime())   ) < 1000)
                        &&
                        (   ((new Date().getTime()) - (ticket.getOutTime().getTime())   ) > - 1000)
        );
        assertTrue(
                (   ticket.getPrice()    < (1 * Fare.CAR_RATE_PER_HOUR) + 0.001   )
                        &&
                        (   ticket.getPrice()    > (1 * Fare.CAR_RATE_PER_HOUR) - 0.001   )
        );
    }

    @Test
    public void testParkingLotExitRecurringUser(){

        ParkingService parkingService = new ParkingService(fareCalculatorService, inputReaderUtil, parkingSpotDAO, ticketDAO);

        parkingService.processIncomingVehicle();
        parkingService.processExitingVehicle();

        parkingService.processIncomingVehicle();
        Ticket ticket = ticketDAO.getTicket("ABCDEF");
        ticketDAO.addOneHourToTicket(ticket);
        parkingService.processExitingVehicle();

        ticket = ticketDAO.getTicket("ABCDEF");

        assertTrue(
                (   ticket.getPrice()    < (1 * 0.95 * Fare.CAR_RATE_PER_HOUR) + 0.001   )
                        &&
                        (   ticket.getPrice()    > (1 * 0.95 * Fare.CAR_RATE_PER_HOUR) - 0.001   )
        );
    }

}
