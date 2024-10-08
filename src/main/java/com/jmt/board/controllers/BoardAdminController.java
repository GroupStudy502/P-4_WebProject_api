package com.jmt.board.controllers;

import com.jmt.board.entities.BoardData;
import com.jmt.board.services.BoardDeleteService;
import com.jmt.board.services.BoardInfoService;
import com.jmt.board.services.admin.BoardAdminService;
import com.jmt.global.ListData;
import com.jmt.global.constants.DeleteStatus;
import com.jmt.global.rests.JSONData;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/board/admin")
public class BoardAdminController {

    private final BoardInfoService boardInfoService;
    private final BoardAdminService boardAdminService;
    private final BoardDeleteService deleteService;
    private final JPAQueryFactory queryFactory;


    @GetMapping // 목록 조회
    public JSONData getList(BoardDataSearch search) {
        ListData<BoardData> data = boardInfoService.getList(search, DeleteStatus.ALL);

        return new JSONData(data);
    }


    @PatchMapping("/{mode}") // 목록 수정, 삭제
    public ResponseEntity<Void> updateList(@PathVariable("mode") String mode, @RequestBody RequestAdminList form) {
        boardAdminService.update(mode, form.getItems());
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{mode}/{seq}") // 게시글 하나 수정 , 삭제
    public ResponseEntity<Void> update(@PathVariable("mode") String mode, @PathVariable("seq") Long seq, @RequestBody RequestBoard form) {
        form.setSeq(seq);

        boardAdminService.update(mode, form);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/info/{seq}")
    public JSONData getInfo(Long seq) {
        BoardData item = boardInfoService.get(seq, DeleteStatus.ALL);

        return new JSONData(item);
    }

    @DeleteMapping("/delete/{seq}")
    public JSONData delete(@PathVariable("seq") Long seq) {
        BoardData item = deleteService.delete(seq);

        return new JSONData(item);
    }
    @DeleteMapping("/complete/{seq}")
    public JSONData complete(@PathVariable("seq") Long seq) {
        BoardData item = deleteService.complete(seq);

        return new JSONData(item);
    }

}
