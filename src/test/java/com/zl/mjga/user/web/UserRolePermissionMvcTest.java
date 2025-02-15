package com.zl.mjga.user.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zl.mjga.common.security.HttpFireWallConfig;
import com.zl.mjga.user.domain.model.User;
import com.zl.mjga.user.domain.model.UserDraft;
import com.zl.mjga.user.domain.model.UserFetcher;
import com.zl.mjga.user.domain.repository.UserAggregateRepository;
import java.util.List;
import org.babyfish.jimmer.Page;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

@WebMvcTest(value = {UserController.class})
@Import({HttpFireWallConfig.class, JacksonSerializeConfig.class})
public class UserRolePermissionMvcTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private UserAggregateRepository userAggregateRepository;

  @Test
  @WithMockUser
  void userAggregatePageQuery_withValidRequest_shouldReturnExpectedResultWithPage()
      throws Exception {
    User mockUser =
        UserDraft.$.produce(
            (userDraft) -> {
              userDraft.setId(1L);
              userDraft.setUsername("admin");
              userDraft.setPassword("admin");
            });
    Page<User> mockPageResult = new Page<>(List.of(mockUser), 2, 1);
    when(userAggregateRepository.fetchAggregateWithPageBy(
            any(UserFetcher.class), any(UserPageQueryVm.class), anyInt(), anyInt()))
        .thenReturn(mockPageResult);
    ResultActions resultActions =
        mockMvc.perform(
            get("/users/search?pageIndex=1&pageSize=10&enable=true")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED));
    resultActions.andExpect(status().isOk());
    MvcResult mvcResult = resultActions.andReturn();
    String contentAsString = mvcResult.getResponse().getContentAsString();
    Page page = new ObjectMapper().readValue(contentAsString, Page.class);
    Assertions.assertNotNull(page);
    Assertions.assertEquals(2, page.getTotalRowCount());
    Assertions.assertEquals(1, page.getTotalPageCount());
  }
}
