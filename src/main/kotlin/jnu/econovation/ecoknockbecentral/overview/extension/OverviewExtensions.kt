package jnu.econovation.ecoknockbecentral.overview.extension

import jnu.econovation.ecoknockbecentral.member.model.entity.Member
import jnu.econovation.ecoknockbecentral.overview.model.entity.DefaultOverviewShortcut
import jnu.econovation.ecoknockbecentral.overview.model.entity.OverviewShortcut

fun DefaultOverviewShortcut.toUserShortcut(member: Member): OverviewShortcut {
    return OverviewShortcut.builder()
        .name(this.name)
        .member(member)
        .targetUrl(this.targetUrl)
        .iconUrl(this.iconUrl)
        .sortOrder(this.sortOrder)
        .build()
}