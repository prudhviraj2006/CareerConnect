package com.careerconnect.app.data.utils

object IndiaLocations {

    val statesAndUTs = listOf(
        "Andhra Pradesh",
        "Arunachal Pradesh",
        "Assam",
        "Bihar",
        "Chhattisgarh",
        "Goa",
        "Gujarat",
        "Haryana",
        "Himachal Pradesh",
        "Jharkhand",
        "Karnataka",
        "Kerala",
        "Madhya Pradesh",
        "Maharashtra",
        "Manipur",
        "Meghalaya",
        "Mizoram",
        "Nagaland",
        "Odisha",
        "Punjab",
        "Rajasthan",
        "Sikkim",
        "Tamil Nadu",
        "Telangana",
        "Tripura",
        "Uttar Pradesh",
        "Uttarakhand",
        "West Bengal",
        "Delhi (NCR)",
        "Chandigarh",
        "Jammu and Kashmir",
        "Ladakh",
        "Puducherry"
    )

    val topCities = listOf(
        "Bengaluru",
        "Hyderabad",
        "Pune",
        "Mumbai",
        "Delhi NCR",
        "Noida",
        "Gurugram",
        "Chennai",
        "Kolkata",
        "Ahmedabad",
        "Kochi",
        "Coimbatore",
        "Jaipur",
        "Indore",
        "Chandigarh / Mohali",
        "Visakhapatnam",
        "Tirupati",
        "Gudiyattam",
        "Bhubaneswar",
        "Lucknow",
        "Nagpur",
        "Mysuru",
        "Thiruvananthapuram",
        "Vadodara",
        "Surat",
        "Patna",
        "Bhopal",
        "Dehradun"
    )

    val employmentTypes = listOf(
        "Full-time",
        "Internship",
        "Fresher",
        "Part-time",
        "Contract"
    )

    val workModes = listOf(
        "Onsite",
        "Hybrid",
        "Remote"
    )

    fun isValidIndianPhone(phone: String): Boolean {
        val clean = phone.replace(Regex("[\\s\\-\\(\\)]"), "")
        // Matches +91XXXXXXXXXX, 91XXXXXXXXXX, or 10-digit XXXXXXXXXX starting with 6, 7, 8, or 9
        val regex = Regex("^(\\+91|91)?[6-9]\\d{9}$")
        return regex.matches(clean)
    }

    fun formatIndianPhone(phone: String): String {
        val digits = phone.replace(Regex("[^0-9]"), "")
        return when {
            digits.length == 10 -> "+91 $digits"
            digits.length == 12 && digits.startsWith("91") -> "+91 ${digits.substring(2)}"
            else -> phone
        }
    }

    fun formatIndianSalary(salaryRaw: String?): String {
        if (salaryRaw.isNullOrBlank()) return "Salary not disclosed"
        val trimmed = salaryRaw.trim()
        if (trimmed.startsWith("₹") || trimmed.contains("LPA", ignoreCase = true) || trimmed.contains("INR", ignoreCase = true)) {
            return trimmed
        }
        // If numeric only or contains numbers
        val numeric = trimmed.replace(Regex("[^0-9]"), "").toLongOrNull()
        if (numeric != null) {
            return when {
                numeric >= 100000 -> "₹${String.format("%.1f", numeric / 100000.0)} LPA"
                numeric in 10000..99999 -> "₹$numeric/month"
                else -> "₹$numeric"
            }
        }
        return trimmed
    }
}
