package ssafy.ddada.api.manager;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import ssafy.ddada.api.CommonResponse;
import ssafy.ddada.api.manager.response.ManagerDetailResponse;
import ssafy.ddada.api.match.request.MatchResultRequest;
import ssafy.ddada.api.match.response.MatchDetailResponse;
import ssafy.ddada.api.match.response.MatchSimpleResponse;
import ssafy.ddada.common.util.SecurityUtil;
import ssafy.ddada.domain.manager.service.ManagerService;
import ssafy.ddada.domain.match.service.MatchService;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/manager")
public class ManagerController {

    private final ManagerService managerService;
    private final MatchService matchService;

    @Operation(summary = "매니저 세부 조회", description = "매니저 세부 정보를 조회하는 api입니다.")
    @GetMapping
    public CommonResponse<ManagerDetailResponse> getManager(){
        Long managerId = SecurityUtil.getLoginMemberId();
        log.info("매니저 세부 조회 >>>> 매니저 ID: {}", managerId);

        ManagerDetailResponse response = managerService.getManagerById(managerId);
        return CommonResponse.ok(response);
    }

    @Operation(summary = "매니저 할당 경기 리스트 조회", description = "매니저에게 할당된 경기 리스트를 조회하는 api입니다.")
    @GetMapping("/matches")
    public CommonResponse<Page<MatchSimpleResponse>> getAllocatedMatches(@RequestParam("page") Integer page, @RequestParam("size") Integer size){
        Long managerId = SecurityUtil.getLoginMemberId();
        log.info("매니저 경기 리스트 조회 >>>> 매니저 ID: {}", managerId);

        Page<MatchSimpleResponse> response = matchService.getMatchesByManagerId(managerId, page, size);
        return CommonResponse.ok(response);
    }

    @Operation(summary = "할당된 경기 저장", description = "경기 종료 후 경기 데이터를 저장하는 api입니다.")
    @PutMapping("/matches/{match_id}")
    public CommonResponse<?> saveMatch(@PathVariable("match_id") Long matchId, @RequestBody MatchResultRequest request){
//        TODO: 이건 매니저 검사용으로만 쓰기. managerId는 비즈니스 로직상 안씀
        Long managerId = SecurityUtil.getLoginMemberId();
        log.info("할당된 경기 저장 >>>> 매니저 ID: {}, 경기 ID: {}", managerId, matchId);

//        TODO: 경기 Request 받아 저장 기능 만들기
        matchService.saveMatch(matchId, request.toCommand());
        return CommonResponse.ok(null, "저장되었습니다.");
    }

}
