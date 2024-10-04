package ssafy.ddada.domain.court.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ssafy.ddada.api.court.response.CourtDetailResponse;
import ssafy.ddada.api.court.response.CourtSimpleResponse;
import ssafy.ddada.common.exception.gym.CourtNotFoundException;
import ssafy.ddada.common.util.S3Util;
import ssafy.ddada.domain.court.command.CourtSearchCommand;
import ssafy.ddada.domain.court.entity.Court;
import ssafy.ddada.domain.court.entity.CourtDocument;
import ssafy.ddada.domain.court.entity.Gym;
import ssafy.ddada.domain.court.repository.CourtElasticsearchRepository;
import ssafy.ddada.domain.court.repository.CourtRepository;

import java.util.List;
import java.util.Objects;
import java.util.Set;

import static ssafy.ddada.common.util.ParameterUtil.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class CourtServiceImpl implements CourtService {

    private final CourtRepository courtRepository;
    private final CourtElasticsearchRepository courtElasticsearchRepository;
    private final ElasticsearchOperations elasticsearchOperations;
    private final S3Util s3Util;

    @Override
    @Transactional(readOnly = true)
    public CourtDetailResponse getCourtById(Long courtId) {
        Court court = courtRepository.findCourtWithMatchesById(courtId)
                .orElseThrow(CourtNotFoundException::new);
        String presignedUrl = s3Util.getPresignedUrlFromS3(court.getGym().getImage());  // S3Util의 메서드 사용
        return CourtDetailResponse.from(court, presignedUrl);
    }

    @Override
    public Page<CourtSimpleResponse> getElasticFilteredCourts(CourtSearchCommand command) {
        String keyword = nullToBlank(command.keyword());
        Set<String> regions = nullToEmptySet(command.regions());

        Criteria criteria = new Criteria();
        if (!isEmptyString(keyword)) {
            criteria = criteria.or("gymName").matches(keyword)
                    .or("gymAddress").matches(keyword);
        }
        if (!isEmptySet(regions)) {
            log.info("regions: {}", regions);
            criteria = criteria.and("gymRegion").in(regions);
        }

        CriteriaQuery query = new CriteriaQuery(criteria).setPageable(command.pageable());
        SearchHits<CourtDocument> courtDocuments = elasticsearchOperations.search(query, CourtDocument.class);
        List<Long> courtIds = courtDocuments
                .map(searchHit -> searchHit.getContent().getCourtId())
                .toList();
        List<CourtSimpleResponse> courts = courtRepository.findCourtsByCourtIds(courtIds)
                .stream()
                .map(court -> {
                    String image = Objects.requireNonNull(court.getGym().getImage());
                    String presignedUrl = s3Util.getPresignedUrlFromS3(image);
                    return CourtSimpleResponse.from(court, presignedUrl);
                })
                .toList();

        return new PageImpl<>(courts, command.pageable(), courtDocuments.getTotalHits());
    }

    @Override
    public void indexAll() {
        List<Court> courts = courtRepository.findAll();
        int size = courts.size(), cur = 0;

        for (Court court : courts) {
            indexCourt(court);
            log.info("코트 인덱싱 진행도: {}%", Math.round(1000.0 * ++cur / size) / 10.0);
        }
    }

    private void indexCourt(Court court) {
        Gym gym = court.getGym();
        CourtDocument courtDocument = CourtDocument.builder()
                .id(String.valueOf(court.getId()))
                .courtId(court.getId())
                .gymName(gym != null ? gym.getName() : null)
                .gymAddress(gym != null ? gym.getAddress() : null)
                .build();

        courtElasticsearchRepository.save(courtDocument);
    }

}

