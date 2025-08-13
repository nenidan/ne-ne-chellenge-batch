package hello.batch.admin.model.dto.outer;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class UserDto {

        private Long id;

        private String email;

        private String role;

        private String nickname;

        private LocalDate birth;

        private String bio;

        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private LocalDateTime deletedAt;
}
