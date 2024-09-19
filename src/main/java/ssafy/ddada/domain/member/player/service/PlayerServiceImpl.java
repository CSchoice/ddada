package ssafy.ddada.domain.member.player.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ssafy.ddada.api.member.player.response.PlayerDetailResponse;
import ssafy.ddada.api.member.player.response.PlayerSignupResponse;
import ssafy.ddada.common.exception.exception.player.MemberNotFoundException;
import ssafy.ddada.common.exception.exception.security.NotAuthenticatedException;
import ssafy.ddada.common.exception.exception.token.TokenSaveFailedException;
import ssafy.ddada.common.util.S3Util;
import ssafy.ddada.common.util.SecurityUtil;
import ssafy.ddada.config.auth.JwtProcessor;
import ssafy.ddada.domain.member.player.command.MemberSignupCommand;
import ssafy.ddada.domain.member.player.command.UpdateProfileCommand;
import ssafy.ddada.domain.member.player.entity.Player;
import ssafy.ddada.domain.member.common.MemberRole;
import ssafy.ddada.domain.member.player.repository.PlayerRepository;


@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PlayerServiceImpl implements PlayerService {

    private final PlayerRepository playerRepository;
    private final JwtProcessor jwtProcessor;
    private final PasswordEncoder passwordEncoder;
    private final S3Util s3Util;

    @Override
    @Transactional
    public PlayerSignupResponse signupMember(MemberSignupCommand signupCommand) {
        Player tempPlayer = playerRepository.findByEmail(signupCommand.email())
                .orElse(null);

        if (tempPlayer == null) {
            tempPlayer = new Player(
                    signupCommand.email(),
                    signupCommand.gender(),
                    signupCommand.birth(),
                    signupCommand.nickname(),
                    passwordEncoder.encode(signupCommand.password()),
                    null,
                    signupCommand.number(),
                    signupCommand.description(),
                    0,
                    MemberRole.PLAYER
            );
            playerRepository.save(tempPlayer);
        }

        // 이미지 처리 로직 추가: MultipartFile이 비어 있으면 기본 이미지 사용
        String imageUrl = (signupCommand.imageUrl() == null || signupCommand.imageUrl().isEmpty())
                ? "https://ddada-image.s3.ap-northeast-2.amazonaws.com/profileImg/default.jpg" // 기본 이미지 경로
                : s3Util.uploadImageToS3(signupCommand.imageUrl(), tempPlayer.getId(), "profileImg/"); // 이미지가 있으면 S3에 업로드

        String encodedPassword = passwordEncoder.encode(signupCommand.password());

        Player signupPlayer = tempPlayer.signupMember(signupCommand, imageUrl, encodedPassword);
        try {
            String accessToken = jwtProcessor.generateAccessToken(signupPlayer);
            String refreshToken = jwtProcessor.generateRefreshToken(signupPlayer);
            jwtProcessor.saveRefreshToken(accessToken, refreshToken);
            return PlayerSignupResponse.of(accessToken, refreshToken);
        } catch (Exception e) {
            throw new TokenSaveFailedException();
        }
    }

    @Override
    public PlayerDetailResponse getMemberDetail() {
        Player currentLoggedInPlayer = getCurrentLoggedInMember();
        String profileImagePath = currentLoggedInPlayer.getProfileImg();

        String preSignedProfileImage = "";
        if (profileImagePath != null) {
            String imagePath = profileImagePath.substring(profileImagePath.indexOf("profileImg/"));
            preSignedProfileImage = s3Util.getPresignedUrlFromS3(imagePath);
        }

        return PlayerDetailResponse.of(
                preSignedProfileImage,
                currentLoggedInPlayer.getNickname(),
                currentLoggedInPlayer.getGender()
        );
    }

    @Override
    @Transactional
    public PlayerDetailResponse updateMemberProfile(UpdateProfileCommand command) {
        Long userId = SecurityUtil.getLoginMemberId()
                .orElseThrow(NotAuthenticatedException::new);
        Player currentLoggedInPlayer = playerRepository.findById(userId)
                .orElseThrow(MemberNotFoundException::new);

        String imageUrl;

        // 만약 MultipartFile이 비어 있으면 기존 이미지 사용, 그렇지 않으면 새 이미지 업로드
        if (command.profileImagePath() == null || command.profileImagePath().isEmpty()) {
            imageUrl = currentLoggedInPlayer.getProfileImg(); // 기존 이미지 사용
        } else {
            // 새 이미지를 업로드하고 새로운 이미지 URL을 얻음
            imageUrl = s3Util.uploadImageToS3(command.profileImagePath(), currentLoggedInPlayer.getId(), "profileImg/");
        }

        // presigned URL 생성
        String presignedUrl = "";
        if (imageUrl != null) {
            String imagePath = imageUrl.substring(imageUrl.indexOf("profileImg/"));
            presignedUrl = s3Util.getPresignedUrlFromS3(imagePath);
        }

        // 프로필 정보 업데이트
        currentLoggedInPlayer.updateProfile(command.nickname(), imageUrl);
        playerRepository.save(currentLoggedInPlayer);

        // presignedUrl을 반환
        return PlayerDetailResponse.of(
                presignedUrl, // 프리사인드 URL 전달
                currentLoggedInPlayer.getNickname(),
                currentLoggedInPlayer.getGender()
        );
    }

    @Override
    @Transactional
    public String deleteMember() {
        getCurrentLoggedInMember().delete();
        return "회원 탈퇴가 성공적으로 처리되었습니다.";
    }

    @Override
    public Boolean checkNickname(String nickname) {
        boolean isDuplicated = playerRepository.existsByNickname(nickname);
        log.debug(">>> 닉네임 중복 체크: {}, 중복 여부: {}", nickname, isDuplicated);
        return isDuplicated;
    }

    private Player getCurrentLoggedInMember() {
        Long userId = SecurityUtil.getLoginMemberId()
                .orElseThrow(NotAuthenticatedException::new);
        return playerRepository.findById(userId)
                .orElseThrow(MemberNotFoundException::new);
    }
}

