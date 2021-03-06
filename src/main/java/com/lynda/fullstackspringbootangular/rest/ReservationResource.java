package com.lynda.fullstackspringbootangular.rest;

import com.lynda.fullstackspringbootangular.converter.RoomEntityToReservableRoomResponseConverter;
import com.lynda.fullstackspringbootangular.entity.ReservationEntity;
import com.lynda.fullstackspringbootangular.entity.RoomEntity;
import com.lynda.fullstackspringbootangular.model.request.ReservationRequest;
import com.lynda.fullstackspringbootangular.model.response.ReservableRoomResponse;
import com.lynda.fullstackspringbootangular.model.response.ReservationResponse;
import com.lynda.fullstackspringbootangular.repository.PageableRoomRepository;
import com.lynda.fullstackspringbootangular.repository.ReservationRepository;
import com.lynda.fullstackspringbootangular.repository.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Optional;

@RestController
@RequestMapping(ResourceConstants.ROOM_RESERVATION_V1)
@CrossOrigin
public class ReservationResource {

    @Autowired
    PageableRoomRepository pageableRoomRepository;

    @Autowired
    RoomRepository roomRepository;

    @Autowired
    ConversionService conversionService;

    @Autowired
    ReservationRepository reservationRepository;

    @RequestMapping(path = "", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Page<ReservableRoomResponse> getAvailableRooms(
            @RequestParam(value = "checkin")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                    LocalDate checkin,
            @RequestParam(value = "checkout")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                    LocalDate checkout, Pageable pageable) {

        Page<RoomEntity> roomEntityList = pageableRoomRepository.findAll(pageable);
        return roomEntityList.map(roomEntity -> new RoomEntityToReservableRoomResponseConverter().convert(roomEntity));
    }

    @RequestMapping(path = "/{roomId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<RoomEntity> getRoomById(
            @PathVariable
                    Long roomId) {

        Optional<RoomEntity> roomEntityOptional = roomRepository.findById(roomId);

        return new ResponseEntity<>(roomEntityOptional.get(), HttpStatus.OK);
    }

    @RequestMapping(path = "", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<ReservationResponse> createReservation(
            @RequestBody
                    ReservationRequest reservationRequest) {

        ReservationEntity reservationEntity = conversionService.convert(reservationRequest, ReservationEntity.class);
        reservationRepository.save(reservationEntity);

        Optional<RoomEntity> roomEntityOptional = roomRepository.findById(reservationRequest.getRoomId());

        RoomEntity roomEntity = roomEntityOptional.get();
        roomEntity.addReservationEntity(reservationEntity);
        roomRepository.save(roomEntity);
        reservationEntity.setRoomEntity(roomEntity);

        ReservationResponse reservationResponse =
                conversionService.convert(reservationEntity, ReservationResponse.class);

        return new ResponseEntity<>(reservationResponse, HttpStatus.CREATED);
    }

    @RequestMapping(path = "", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<ReservableRoomResponse> updateReservation(
            @RequestBody
                    ReservationRequest reservationRequest
    ) {
        return new ResponseEntity<>(new ReservableRoomResponse(), HttpStatus.OK);
    }

    @RequestMapping(path = "/{reservationId}", method = RequestMethod.DELETE)
    public ResponseEntity<Void> deleteReservation(
            @PathVariable
                    long reservationId) {

        return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
    }
}
