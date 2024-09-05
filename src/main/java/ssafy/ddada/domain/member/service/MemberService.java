package ssafy.ddada.domain.member.service;

import org.springframework.web.multipart.MultipartFile;
import ssafy.ddada.api.member.response.MemberDetailResponse;
import ssafy.ddada.api.member.response.MemberLoginResponse;
import ssafy.ddada.api.member.response.MemberSignupResponse;
import ssafy.ddada.domain.member.command.MemberLoginCommand;
import ssafy.ddada.domain.member.command.MemberSignupCommand;
import ssafy.ddada.domain.member.command.UpdateProfileCommand;

public interface MemberService {
    MemberSignupResponse signupMember(MemberSignupCommand signupCommand);
    MemberDetailResponse getMemberDetail();
    MemberDetailResponse updateMemberProfile(UpdateProfileCommand command);
    String deleteMember();
//    MemberLoginResponse loginMember(MemberLoginCommand command);
}
