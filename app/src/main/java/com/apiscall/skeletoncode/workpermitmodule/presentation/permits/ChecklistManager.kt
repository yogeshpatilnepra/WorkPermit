package com.apiscall.skeletoncode.workpermitmodule.presentation.permits

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import com.google.android.material.checkbox.MaterialCheckBox

data class ChecklistItem(
    val id: String, val text: String, val isRequired: Boolean = true, var isChecked: Boolean = false
)

object ChecklistManager {

    fun getChecklistItems(permitType: String): List<ChecklistItem> {
        return when (permitType.lowercase()) {
            "hot work" -> listOf(
                ChecklistItem("gas_testing", "Gas testing (LEL must be < 10%)"),
                ChecklistItem(
                    "fire_watch",
                    "Fire watch present during work and for 30-60 minutes after work ends"
                ),
                ChecklistItem("spark_shields", "Use of spark shields / spark arrestors"),
                ChecklistItem(
                    "combustibles_removed",
                    "Removal or covering of all combustible materials in the work area"
                ),
                ChecklistItem(
                    "barricading", "Proper barricading and warning signs around the hot-work zone"
                )
            )

            "loto" -> listOf(
                ChecklistItem("isolation_points", "Complete list of all energy isolation points"),
                ChecklistItem("locks_applied", "Locks and tags applied to each isolation point"),
                ChecklistItem("locks_verified", "Locks/tags verified by authorized person"),
                ChecklistItem(
                    "zero_energy_test", "'Try to start / test zero energy' performed and confirmed"
                ),
                ChecklistItem("hidden_sources", "Check for multiple / hidden energy sources")
            )

            "confined space" -> listOf(
                ChecklistItem("oxygen_level", "Oxygen level: 19.5% – 23.5%"),
                ChecklistItem("lel_level", "LEL: < 10%"),
                ChecklistItem(
                    "toxic_gases", "H₂S, CO and other toxic gases within acceptable limits"
                ),
                ChecklistItem("ventilation", "Adequate ventilation plan and equipment in place"),
                ChecklistItem(
                    "rescue_equipment", "Rescue harness, tripod, and retrieval line ready"
                ),
                ChecklistItem("attendant", "Attendant assigned and log maintained"),
                ChecklistItem("rescue_plan", "Emergency rescue plan documented")
            )

            "working at height" -> listOf(
                ChecklistItem(
                    "harness_inspection",
                    "Inspection of fall arrest harness, lanyard, and connectors"
                ), ChecklistItem(
                    "anchor_points", "Anchor points rated and certified (≥ 5,000 lbs / 22 kN)"
                ), ChecklistItem(
                    "fall_protection",
                    "Guardrails, safety nets, or personal fall protection system in place"
                ), ChecklistItem(
                    "scaffolding", "Scaffolding / ladder / platform inspection completed"
                ), ChecklistItem("rescue_plan_height", "Rescue plan for suspended worker")
            )

            "lifting" -> listOf(
                ChecklistItem("load_chart", "Load chart / capacity verification for crane / hoist"),
                ChecklistItem(
                    "rigging_inspection",
                    "Rigging gear (slings, shackles, hooks) inspected and certified"
                ),
                ChecklistItem("qualified_crew", "Qualified rigger and signalman assigned"),
                ChecklistItem("drop_zone", "Exclusion / drop zone established and barricaded"),
                ChecklistItem(
                    "wind_speed", "Wind speed monitoring and maximum allowable limit checked"
                ),
                ChecklistItem("lift_plan", "Lift plan reviewed (if critical lift)")
            )

            "live equipment" -> listOf(
                ChecklistItem(
                    "arc_flash_assessment",
                    "Arc flash risk assessment completed and PPE category determined"
                ),
                ChecklistItem(
                    "arc_rated_ppe", "Appropriate arc-rated PPE and insulated tools used"
                ),
                ChecklistItem(
                    "live_work_procedure", "Approved live work procedure / risk assessment in place"
                ),
                ChecklistItem("voltage_testing", "Voltage testing before and during work"),
                ChecklistItem(
                    "boundaries", "Shock protection boundary and arc flash boundary established"
                )
            )

            "cold work" -> listOf(
                ChecklistItem("basic_isolation", "Basic isolation (if any mechanical risk exists)"),
                ChecklistItem("correct_ppe", "Correct PPE for the task"),
                ChecklistItem("barricading_cold", "Barricading / signage as needed"),
                ChecklistItem(
                    "spill_prevention", "Spill prevention / control measures (if applicable)"
                ),
                ChecklistItem("housekeeping", "Housekeeping and safe access maintained")
            )

            else -> emptyList()
        }
    }

    fun inflateChecklistItems(
        container: LinearLayout, items: List<ChecklistItem>, layoutInflater: LayoutInflater
    ) {
        container.removeAllViews()
        items.forEach { item ->
            val checkBox = MaterialCheckBox(container.context).apply {
                text = item.text
                isChecked = item.isChecked
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = 8
                }
                setOnCheckedChangeListener { _, isChecked ->
                    item.isChecked = isChecked
                }
            }
            container.addView(checkBox)
        }
    }

    fun areRequiredItemsChecked(items: List<ChecklistItem>): Boolean {
        return items.filter { it.isRequired }.all { it.isChecked }
    }

    fun getCheckedItems(items: List<ChecklistItem>): Map<String, Boolean> {
        return items.associate { it.id to it.isChecked }
    }
}