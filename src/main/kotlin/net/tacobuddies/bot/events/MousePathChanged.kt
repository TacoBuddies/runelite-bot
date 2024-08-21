package net.tacobuddies.bot.events

import com.github.otsoko.bezier.BezierCurve
import lombok.Value

@Value
data class MousePathChanged(val curve: BezierCurve)
