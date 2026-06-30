package jnu.econovation.ecoknockbecentral.whozin.dto

import jnu.econovation.ecoknockbecentral.common.exception.server.InternalServerException
import jnu.econovation.ecoknockbecentral.member.model.vo.Cohort
import jnu.econovation.ecoknockbecentral.whozin.dto.response.WhozinMember
import jnu.econovation.ecoknockbecentral.whozin.dto.response.WhozinMembersByDate
import java.time.Duration
import java.time.LocalDate
import java.util.*

data class WhozinMembersInternalDTO(
    val date: LocalDate,
    val users: List<WhozinUser>
) {
    companion object {
        fun from(
            membersByDate: WhozinMembersByDate
        ): WhozinMembersInternalDTO {
            return WhozinMembersInternalDTO(
                date = parseDate(rawDate = membersByDate.date),
                users = membersByDate.members.map(WhozinUser::from)
            )
        }

        private fun parseDate(rawDate: List<Int>): LocalDate {
            if (rawDate.size != 3) {
                throw InternalServerException(
                    IllegalStateException("예상치 못한 날짜 응답 -> ${rawDate.joinToString(",")}")
                )
            }

            val year = rawDate[0]
            val month = rawDate[1]
            val day = rawDate[2]

            return runCatching {
                LocalDate.of(year, month, day)
            }.getOrElse {
                throw InternalServerException(
                    IllegalStateException("올바르지 않은 날짜 응답 -> ${rawDate.joinToString(",")}", it)
                )
            }
        }
    }
}

data class WhozinUser(
    val userId: UUID,
    val cohort: Cohort,
    val name: String,
    val presenceDuration: Duration
) {
    companion object {
        private val HOUR_REGEX = Regex("""(\d+)\s*시간""")
        private val MINUTE_REGEX = Regex("""(\d+)\s*분""")

        fun from(member: WhozinMember): WhozinUser {
            return WhozinUser(
                userId = member.memberId,
                cohort = Cohort(member.generation),
                name = member.memberName,
                presenceDuration = parseKoreanDuration(member.presenceDuration)
            )
        }

        private fun parseKoreanDuration(value: String): Duration {
            val hour = HOUR_REGEX
                .find(value)
                ?.groupValues
                ?.get(1)

            val minute = MINUTE_REGEX
                .find(value)
                ?.groupValues
                ?.get(1)

            if (hour == null && minute == null) {
                throw InternalServerException(
                    IllegalStateException("예상치 못한 동방에 있는 시간 응답 -> $value")
                )
            }

            return runCatching {
                Duration
                    .ofHours(hour?.toLong() ?: 0L)
                    .plusMinutes(minute?.toLong() ?: 0L)
            }.getOrElse {
                throw InternalServerException(
                    IllegalStateException("올바르지 않은 동방에 있는 시간 응답 -> $value", it)
                )
            }
        }
    }
}