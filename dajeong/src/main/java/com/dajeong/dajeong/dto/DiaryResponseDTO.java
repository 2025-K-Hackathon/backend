package com.dajeong.dajeong.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.List;
import com.dajeong.dajeong.entity.Diary;

@Getter @Setter
@NoArgsConstructor
public class DiaryResponseDTO {
    private Long id;
    private String date;
    private String content;
    private String correctedText;
    private String reply;
    private List<String> incorrectWords;
    private List<String> correctedWords;

    public DiaryResponseDTO(Diary d) {
        this.id = d.getId();
        this.date = d.getDate().toString();
        this.content = d.getContent();
        this.correctedText = d.getCorrectedText();
        this.reply = d.getReply();
        this.incorrectWords = d.getIncorrectWords();
        this.correctedWords = d.getCorrectedWords();
    }
}

