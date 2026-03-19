package com.krish.service;

import com.krish.modal.Reservation;
import com.krish.payload.dto.ReservationDTO;
import com.krish.payload.request.ReservationRequest;
import com.krish.payload.request.ReservationSearchRequest;
import com.krish.payload.response.PageResponse;


public interface ReservationService {

    ReservationDTO createReservation(ReservationRequest reservationRequest) throws Exception;

    ReservationDTO createReservationForUser(ReservationRequest reservationRequest, Long userId) throws Exception;

    ReservationDTO cancelReservation(Long reservationId) throws Exception;

    ReservationDTO fulfillReservation(Long reservationId) throws Exception;

    PageResponse<ReservationDTO> getMyReservations(ReservationSearchRequest searchRequest) throws Exception;

    PageResponse<ReservationDTO> searchReservations(ReservationSearchRequest searchRequest);


}
