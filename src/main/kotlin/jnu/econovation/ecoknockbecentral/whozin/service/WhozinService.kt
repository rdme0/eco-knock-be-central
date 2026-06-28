package jnu.econovation.ecoknockbecentral.whozin.service

import jnu.econovation.ecoknockbecentral.whozin.client.WhozinClient
import jnu.econovation.ecoknockbecentral.whozin.dto.WhozinMembersInternalDTO
import org.springframework.stereotype.Service

@Service
class WhozinService(
    private val client: WhozinClient
) {
    /**
     * Whozin 재실 기록이 있는 회원 목록을 조회한다.
     *
     * [day]가 null이면 [year]-[month]에 해당하는 월 전체 데이터를 조회하고,
     * [day]가 있으면 해당 날짜의 데이터만 조회한다.
     *
     * 반환 목록은 날짜 단위로 묶인다.
     * - 일 단위 조회: 보통 1개 날짜 데이터
     * - 월 단위 조회: 여러 날짜 데이터
     *
     * @param year 조회 연도
     * @param month 조회 월, 1부터 12까지
     * @param day 조회 일자. null이면 해당 월 전체 조회
     * @return 날짜별 Whozin 재실 회원 목록
     */
    fun getWhozinMembers(
        year: Int,
        month: Int,
        day: Int? = null
    ): List<WhozinMembersInternalDTO> {
        return client
            .getWhozinMembers(
                year = year,
                month = month,
                day = day
            )
            .data
            .dates
            .map(transform = WhozinMembersInternalDTO::from)
    }
}