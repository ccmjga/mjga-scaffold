package com.mjga.unit;

import static org.assertj.core.api.Assertions.*;

import com.mjga.dto.PageRequestDto;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;


@ExtendWith(MockitoExtension.class)
public class PageRequestDtoUnitTest {

  @Test
  void setSortBy_whenSortByFieldIsExpectFormat_thenDeserializeCorrect() {
    String sortBy1 = "id asc,name desc";
    String sortBy2 = "id asc";
    String sortBy3 = "id asc,";
    String sortBy4 = ",";
    String sortBy5 = "";
    PageRequestDto pageRequestDto1 = new PageRequestDto();
    PageRequestDto pageRequestDto2 = new PageRequestDto();
    PageRequestDto pageRequestDto3 = new PageRequestDto();
    PageRequestDto pageRequestDto4 = new PageRequestDto();
    PageRequestDto pageRequestDto5 = new PageRequestDto();
    pageRequestDto1.setSortBy(sortBy1);
    pageRequestDto2.setSortBy(sortBy2);
    pageRequestDto3.setSortBy(sortBy3);
    pageRequestDto4.setSortBy(sortBy4);
    pageRequestDto5.setSortBy(sortBy5);
    assertThat(
            pageRequestDto1
                .getSortBy()
                .equals(
                    Map.of(
                        "id", PageRequestDto.Direction.ASC, "name", PageRequestDto.Direction.DESC)))
        .isTrue();
    assertThat(pageRequestDto2.getSortBy().equals(Map.of("id", PageRequestDto.Direction.ASC)))
        .isTrue();
    assertThat(pageRequestDto3.getSortBy().equals(Map.of("id", PageRequestDto.Direction.ASC)))
        .isTrue();
    assertThat(pageRequestDto4.getSortBy().equals(new HashMap<>())).isTrue();
    assertThat(pageRequestDto5.getSortBy().equals(new HashMap<>())).isTrue();
  }

  @Test
  void setSortBy_whenSortByFieldInvalidFormat_thenRaiseError() {
    String sortBy1 = "id bbb";
    String sortBy2 = "2%^ asc";
    String sortBy3 = "id asc,*&23 desc";
    String sortBy4 = "id,name desc";
    String sortBy5 = ",name asc";
    PageRequestDto pageRequestDto = new PageRequestDto();
    assertThatThrownBy(() -> pageRequestDto.setSortBy(sortBy1))
        .isInstanceOf(IllegalArgumentException.class);

    assertThatThrownBy(() -> pageRequestDto.setSortBy(sortBy2))
        .isInstanceOf(IllegalArgumentException.class);

    assertThatThrownBy(() -> pageRequestDto.setSortBy(sortBy3))
        .isInstanceOf(IllegalArgumentException.class);

    assertThatThrownBy(() -> pageRequestDto.setSortBy(sortBy4))
        .isInstanceOf(IllegalArgumentException.class);

    assertThatThrownBy(() -> pageRequestDto.setSortBy(sortBy5))
        .isInstanceOf(IllegalArgumentException.class);
  }
}
