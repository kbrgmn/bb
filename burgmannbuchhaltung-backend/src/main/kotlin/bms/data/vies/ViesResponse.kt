package bms.data.vies


import kotlinx.serialization.Serializable

@Serializable
data class ViesResponse(
    val address: String, // Hugo-Portisch-Gasse 1\nAT-1136 Wien
    val isValid: Boolean, // true
    val name: String, // ORF-Enterprise GmbH & Co KG
    val requestDate: String, // 023-08-29T12:48:02.700Z
    val requestIdentifier: String, // WAPIAAAAYpBTOBpv
    val userError: String, // VALID
    val vatNumber: String, // U46832307
    //val viesApproximate: ViesApproximate
) /*{
    @Serializable
    data class ViesApproximate(
        val city: String, // ---
        val companyType: String, // ---
        val matchCity: Int, // 3
        val matchCompanyType: Int, // 3
        val matchName: Int, // 3
        val matchPostalCode: Int, // 3
        val matchStreet: Int, // 3
        val name: String, // ---
        val postalCode: String, // ---
        val street: String // ---
    )
}*/
