package com.jmt.reservation.services;

import com.jmt.global.exceptions.UnAuthorizedException;
import com.jmt.member.MemberUtil;
import com.jmt.member.entities.Member;
import com.jmt.reservation.constants.ReservationStatus;
import com.jmt.reservation.entities.Reservation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static com.jmt.reservation.constants.ReservationStatus.*;

@Service
@RequiredArgsConstructor
public class ReservationCancelService {
    private final ReservationInfoService infoService;
    private final ReservationStatusService statusService;
    private final MemberUtil memberUtil;

    public Reservation cancel(Long orderNo) {
        Reservation item = infoService.get(orderNo);
        Member member = memberUtil.getMember();
        Member rMember = item.getMember();
        if (!member.getEmail().equals(rMember.getEmail())) {
            throw new UnAuthorizedException();
        }

        ReservationStatus status = item.getStatus();

        if (status == APPLY || status == START) {
            statusService.change(orderNo, CANCEL);
            item.setStatus(CANCEL);
            item.setStatusStr(CANCEL.getTitle());
        } else if (status == CONFIRM) {
            statusService.change(orderNo, REFUND);
            item.setStatus(REFUND);
            item.setStatusStr(REFUND.getTitle());
        }

        return item;
    }
}