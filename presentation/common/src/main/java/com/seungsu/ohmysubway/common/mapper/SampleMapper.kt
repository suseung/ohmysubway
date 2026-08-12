package com.seungsu.ohmysubway.common.mapper

import com.seungsu.ohmysubway.common.model.SampleUiModel
import com.seungsu.ohmysubway.domain.model.SampleDomainModel

fun SampleDomainModel.toUiModel() = SampleUiModel(
    id = id,
    title = title,
    description = description,
    imageUrl = imageUrl,
)

fun SampleUiModel.toDomain() = SampleDomainModel(
    id = id,
    title = title,
    description = description,
    imageUrl = imageUrl,
)
