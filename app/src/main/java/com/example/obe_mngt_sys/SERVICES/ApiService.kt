package com.example.obe_mngt_sys.SERVICES

import com.example.obe_mngt_sys.MODELS.Activity
import com.example.obe_mngt_sys.MODELS.ActivityType
import com.example.obe_mngt_sys.MODELS.AddPLORequest
import com.example.obe_mngt_sys.MODELS.AddPLOResponse
import com.example.obe_mngt_sys.MODELS.AddProgResponce
import com.example.obe_mngt_sys.MODELS.AdvisorInfoResponse
import com.example.obe_mngt_sys.MODELS.AllocationResponse
import com.example.obe_mngt_sys.MODELS.ApiResponse
import com.example.obe_mngt_sys.MODELS.ApiResponseFORUPDATEMARKS
import com.example.obe_mngt_sys.MODELS.ApiResponseclomappingupdation
import com.example.obe_mngt_sys.MODELS.ApprovalResponse
import com.example.obe_mngt_sys.MODELS.AvailableSessionsResponse
import com.example.obe_mngt_sys.MODELS.BasicResponse
import com.example.obe_mngt_sys.MODELS.CLO
import com.example.obe_mngt_sys.MODELS.CLOPLOMappingItem
import com.example.obe_mngt_sys.MODELS.CLORequest
import com.example.obe_mngt_sys.MODELS.CLOTEST
import com.example.obe_mngt_sys.MODELS.CloActivityMapping
import com.example.obe_mngt_sys.MODELS.CloGradeResponse
import com.example.obe_mngt_sys.MODELS.CloMappingResponse
import com.example.obe_mngt_sys.MODELS.CountResponse
import com.example.obe_mngt_sys.MODELS.CoursePLOResponse
import com.example.obe_mngt_sys.MODELS.CourseResponse
import com.example.obe_mngt_sys.MODELS.CourseResultsResponse
import com.example.obe_mngt_sys.MODELS.CourseTask
import com.example.obe_mngt_sys.MODELS.Courses
import com.example.obe_mngt_sys.MODELS.CreateCLOResponse
import com.example.obe_mngt_sys.MODELS.CrsCloMappingRequest
import com.example.obe_mngt_sys.MODELS.DeleteCLOResponse
import com.example.obe_mngt_sys.MODELS.DeletePLOResponse
import com.example.obe_mngt_sys.MODELS.DeleteProgResponse
import com.example.obe_mngt_sys.MODELS.GradeDistributionResponse
import com.example.obe_mngt_sys.MODELS.HomeworkResponse
import com.example.obe_mngt_sys.MODELS.LoginResponse
import com.example.obe_mngt_sys.MODELS.MappingResponse
import com.example.obe_mngt_sys.MODELS.MappingStatusResponse
import com.example.obe_mngt_sys.MODELS.MarkUpdateRequest
import com.example.obe_mngt_sys.MODELS.MarksDistributionRequest
import com.example.obe_mngt_sys.MODELS.MarksDistributionResponse
import com.example.obe_mngt_sys.MODELS.OfferedCourse
import com.example.obe_mngt_sys.MODELS.OfferedCourseDetailsResponse
import com.example.obe_mngt_sys.MODELS.PLO
import com.example.obe_mngt_sys.MODELS.PLOCloMappingResponse
import com.example.obe_mngt_sys.MODELS.PLOResponse
import com.example.obe_mngt_sys.MODELS.PermissionResponse
import com.example.obe_mngt_sys.MODELS.PloMapCloResponse
import com.example.obe_mngt_sys.MODELS.PloResultResponse
import com.example.obe_mngt_sys.MODELS.PostProgram
import com.example.obe_mngt_sys.MODELS.ProgramPloResponse
import com.example.obe_mngt_sys.MODELS.ProgramsResponse
import com.example.obe_mngt_sys.MODELS.QuestionCloMappingsResponse
import com.example.obe_mngt_sys.MODELS.QuestionRequest
import com.example.obe_mngt_sys.MODELS.QuestionResponse
import com.example.obe_mngt_sys.MODELS.SelectedCoursesRequest
import com.example.obe_mngt_sys.MODELS.StatusUpdateResponse
import com.example.obe_mngt_sys.MODELS.StudentAcademicRecord
import com.example.obe_mngt_sys.MODELS.StudentsResponse
import com.example.obe_mngt_sys.MODELS.SuggestionRequest
import com.example.obe_mngt_sys.MODELS.SuggestionResponse
import com.example.obe_mngt_sys.MODELS.SuggestionsCountResponse
import com.example.obe_mngt_sys.MODELS.TaskDeleteResponse
import com.example.obe_mngt_sys.MODELS.TaskDetailsResponse
import com.example.obe_mngt_sys.MODELS.TaskRequest
import com.example.obe_mngt_sys.MODELS.TaskResponse
import com.example.obe_mngt_sys.MODELS.TaskResultsResponse
import com.example.obe_mngt_sys.MODELS.TaskWithQuestionsUpdateRequest
import com.example.obe_mngt_sys.MODELS.TeacherCoursesBySessionResponse
import com.example.obe_mngt_sys.MODELS.TeacherDashboardResponse
import com.example.obe_mngt_sys.MODELS.TeacherMappingResponse
import com.example.obe_mngt_sys.MODELS.UnmappedCLO
import com.example.obe_mngt_sys.MODELS.UpdateCLORequest
import com.example.obe_mngt_sys.MODELS.UpdateCLOResponse
import com.example.obe_mngt_sys.MODELS.UpdatePLORequest
import com.example.obe_mngt_sys.MODELS.UpdatePLOResponse
import com.example.obe_mngt_sys.MODELS.UpdatePercentageRequest
import com.example.obe_mngt_sys.MODELS.UpdatePercentageResponse
import com.example.obe_mngt_sys.MODELS.UpdateProgResponse
import com.example.obe_mngt_sys.MODELS.md_respone
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Query

interface ApiService {
    // Login API
    @GET("api/OBE/login")
    fun login(
        @Query("username") username: String,
        @Query("password") password: String
    ): Call<LoginResponse>

    // Programs API
    @GET("api/OBE/GetHODPrograms")
    fun getHODPrograms(
        @Query("hodId") hodId: String,
    ): Call<ProgramsResponse>

    // 🔹 POST request to add a new program
    @POST("api/OBE/AddProgram")
    fun addProgram(@Body newProgram: PostProgram): Call<AddProgResponce>

    @PUT("api/OBE/UpdateProgram")
    fun updateProgram(
        @Query("programId") programId: String,
        @Body updatedProgram: PostProgram
    ): Call<UpdateProgResponse>

    // Delete Program with programId in query parameter
    @DELETE("api/OBE/DeleteProgram")
    fun deleteProgram(
        @Query("programId") programId: String
    ): Call<DeleteProgResponse>


    // Courses API
    @GET("api/OBE/GetCoursesByProgramId")
    fun getCoursesByProgramId(
        @Query("programId") programId: Int,
    ): Call<List<Courses>>

    @POST("api/OBE/AddCourseToProgram")
    fun AddCourseToProgram(
        @Query("C_code") courseCode: String,
        @Query("cname") courseName: String,
        @Query("chr") creditHours: Int,
        @Query("lab") lab: String,
        @Query("P_ID") programId: Int,
        @Query("pre") prerequisite: String
    ): Call<Void>

    // Add this to your Retrofit interface
    @DELETE("api/OBE/DeleteCourseFromProgram")
    fun deleteCourseFromProgram(
        @Query("C_code") courseCode: String
    ): Call<Void>

    // Get courses not associated with a program
    @GET("api/OBE/GetCoursesNotInProgram")
    fun getCoursesNotInProgram(
        @Query("programId") programId: Int
    ): Call<List<CourseResponse>>

    @POST("api/OBE/AddSelectedCoursesToProgram")
    fun addSelectedCoursesToProgram(
        @Body request: SelectedCoursesRequest
    ): Call<Void>


    @GET("api/OBE/GetPLOsByProgramId")
    suspend fun getPLOsByProgramId(
        @Query("programId") programId: Int
    ): Response<List<PLO>>

    @POST("api/OBE/AddPLO")
    suspend fun addPLO(@Body request: AddPLORequest): AddPLOResponse

    // Fetch PLOs not in the current program
    @GET("api/OBE/GetPLOsNotInProgram")
    suspend fun getPLOsNotInProgram(
        @Query("programId") programId: Int
    ): List<PLO>

    // Add selected PLOs to the current program
    @POST("api/OBE/AddSelectedPLOsToProgram")
    suspend fun addSelectedPLOsToProgram(
        @Query("programId") programId: Int,
        @Body selectedPLOIds: List<Int>
    ): Response<Void>


    @PUT("api/OBE/UpdatePLO")
    suspend fun updatePLO(
        @Query("ploId") ploId: Int,
        @Body request: UpdatePLORequest
    ): UpdatePLOResponse  // Return response body directly

    @DELETE("api/OBE/DeletePLOAssociations")
    suspend fun deletePLO(
        @Query("ploId") ploId: Int,
        @Query("pId") pId: Int
    ): DeletePLOResponse

    // 🔹 API to Get Courses with PLO Mapping
    @GET("api/OBE/GetCoursesWithMappedPLOs")
    suspend fun GetCoursesWithMappedPLOs(
        @Query("programId") programId: Int
    ): Response<List<CoursePLOResponse>>

    @PUT("api/OBE/updateCourseMapPlo")
    suspend fun updateCourseMapPlo(
        @Query("P_ID") P_ID: Int,
        @Query("C_code") C_code: String,
        @Query("PLO_ID") PLO_ID: Int,
        @Query("per") per: Int
    ): Response<Unit>


    @GET("api/OBE/GetOfferedCourses")
    suspend fun getOfferedCourses(@Query("P_ID") programId: Int): Response<List<OfferedCourse>>

    @POST("api/OBE/AllocateTeacher")
    suspend fun allocateTeacher(
        @Query("oc_id") ocId: Int,
        @Query("T_id") teacherId: String
    ): Response<AllocationResponse>


    //===============================================TEACHER SIDE ===============


    @GET("api/Teacher/TeacherDashboard")
    suspend fun TeacherDashboard(
        @Query("teacherId") teacherId: String
    ): Response<TeacherDashboardResponse>

    @GET("api/Teacher/GetAvailableSessions")
    suspend fun GetAvailableSessions(
        @Query("teacherId") teacherId: String
    ): Response<AvailableSessionsResponse>

    @GET("api/Teacher/GetTeacherCoursesBySession")
    suspend fun GetTeacherCoursesBySession(
        @Query("teacherId") teacherId: String,
        @Query("session") session: String,
        @Query("year") year: Int
    ): Response<TeacherCoursesBySessionResponse>


//
//    @GET("api/Teacher/GetCLOsByCourseSectionAndSemester")
//    suspend fun GetCLOsByCourseSectionAndSemester(
//        @Query("courseCode") courseCode: String,
//        @Query("section") section: String,
//        @Query("semester") semester: Int
//    ): Response<CLOResponse>


    @GET("api/Teacher/GetCLOs")
    suspend fun GetCLOsByOfferedCourseId(
        @Query("oc_id") ocId: Int
    ): Response<List<CLO>>


    @GET("api/Teacher/GetPloMapCloRecords")
    suspend fun getCLOPLOMapping(
        @Query("oc_id") ocId: Int
    ): Response<List<CLOPLOMappingItem>>

    @POST("api/Teacher/CreateCLO")
    suspend fun CreateCLO(
        @Body request: CLORequest  // Send as JSON body instead of query params
    ): Response<CreateCLOResponse>


    @PUT("api/Teacher/UpdateCLO/{cloId}")
    suspend fun updateCLO(
        @Query("cloId") cloId: Int,
        @Body request: UpdateCLORequest
    ): UpdateCLOResponse

    @DELETE("api/Teacher/DeleteCLO/{cloId}")
    suspend fun deleteCLO(@Query("cloId") cloId: Int): DeleteCLOResponse


    @GET("api/Teacher/GetCLOsNotInOC")
    suspend fun getCLOsNotInCourse(
        @Query("ocId") ocId: Int  // Note parameter name matches backend (ocId vs oc_id)
    ): Response<List<UnmappedCLO>>  // New model


    // For AddCrsClo (replacing addMultipleCLOs)
    @POST("api/Teacher/AddCrsClo")
    suspend fun AddCrsClo(
        @Body request: CrsCloMappingRequest
    ): Response<MappingResponse>

    @GET("api/Teacher/CheckPermission")
    suspend fun CheckPermission(
        @Query("oc_id") ocId: Int,
        @Query("T_id") teacherId: String
    ): Response<PermissionResponse>


    @POST("api/Teacher/AddPloMapClo")
    suspend fun AddPloMapClo(
        @Query("o_id") offeredCourseId: Int,
        @Query("clo_id") cloId: Int,
        @Query("plo_id") ploId: Int,
        @Query("per") percentage: Float
    ): Response<Unit>


    @PUT("api/Teacher/UpdateStatus")
    suspend fun updateMappingStatus(
        @Query("oc_id") ocId: Int
    ): Response<StatusUpdateResponse>


    // -------------------------HOD SIDE MAPPING FOR APPROVALS--------------------------------

    @GET("api/OBE/GetTeacherMappingsByProgram")
    suspend fun getTeacherMappingsByProgram(
        @Query("programId") programId: Int
    ): Response<List<TeacherMappingResponse>>

    @GET("api/OBE/GetMappingPloCloFORHODView")
    suspend fun getPloCloMappingForHodView(
        @Query("oc_id") offeredCourseId: Int
    ): Response<List<PLOCloMappingResponse>>


    @PUT("api/OBE/ApproveMapping")
    suspend fun approveMapping(@Query("oc_id") ocId: Int): Response<ApprovalResponse>


    @POST("api/OBE/SendSuggestion")
    suspend fun sendSuggestion(
        @Body suggestion: SuggestionRequest
    ): Response<BasicResponse>


//---------------------------------------------------------------------------------------------------


    // Again teacher Side
    @GET("api/Teacher/GetMappingStatus")
    suspend fun GetMappingStatus(
        @Query("oc_id") ocId: Int
    ): Response<MappingStatusResponse>


    @GET("api/Teacher/GetCourseSuggestions")
    suspend fun getCourseSuggestions(
        @Query("offeredCourseId") offeredCourseId: Int
    ): Response<SuggestionResponse>


    @GET("api/Teacher/GetCourseSuggestionsCount")
    suspend fun GetCourseSuggestionsCount(
        @Query("offeredCourseId") offeredCourseId: Int
    ): Response<SuggestionsCountResponse>


    @GET("api/Teacher/GetCloActivityMappings")
    suspend fun getCloActivityMappings(
        @Query("oc_id") offeredCourseId: Int
    ): Response<List<CloActivityMapping>>


    @POST("api/Teacher/AddOrUpdateCloActivityMapping")
    suspend fun addOrUpdateCloActivityMapping(
        @Query("oc_id") offeredCourseId: Int,
        @Query("clo_id") cloId: Int,
        @Query("type_id") typeId: Int,
        @Query("per") percentage: Int
    ): Response<Unit>

    @GET("api/Teacher/getTasksByOfferID")
    suspend fun getTasksByOfferID(
        @Query("oc_id") ocId: Int
    ): Response<List<CourseTask>>


//        @GET("api/Teacher/GetMainActivities")
//    suspend fun getMainActivities(): Response<List<Activity>>

    @GET("api/Teacher/GetMainActivities")
    suspend fun getMainActivities(
        @Query("oc_id") ocId: Int,
        @Query("T_id") teacherId: String
    ): Response<List<Activity>>


    @GET("api/Teacher/GetSubtypes")
    suspend fun getSubtypes(
        @Query("mainActivityId") mainActivityId: Int
    ): Response<List<Activity>>
//    @GET("api/Teacher/GetMainActivities")
//    suspend fun getMainActivities(
//        @Query("oc_id") ocId: Int,
//        @Query("T_id") teacherId: String
//    ): Response<List<Activity>>
//
//    @GET("api/Teacher/GetSubtypes")
//    suspend fun getSubtypes(
//        @Query("mainActivityId") mainActivityId: Int
//    ): Response<List<Activity>>


    @POST("api/Teacher/AddTask")
    suspend fun addTask(@Body request: TaskRequest): Response<TaskResponse>

    @DELETE("api/Teacher/DeleteTask")
    suspend fun deleteTask(@Query("tsk_id") taskId: Int): Response<TaskDeleteResponse>

    @POST("api/Teacher/AddQuestion")
    suspend fun addQuestion(@Body request: QuestionRequest): Response<QuestionResponse>





    @GET("api/Teacher/GetQuestionCloMappingsByActivityType")
    suspend fun getQuestionCloMappingsByActivityType(
        @Query("oc_id") ocId: Int,
        @Query("typeId") typeId: Int
    ): Response<QuestionCloMappingsResponse>


    @POST("api/Teacher/UpdateQuestionCloMappingPercentage")
    suspend fun updateQuestionCloMappingPercentage(
        @Body request: UpdatePercentageRequest
    ): Response<UpdatePercentageResponse>


    @GET("api/Teacher/GetTaskDetails")
    suspend fun getTaskDetails(
        @Query("tsk_id") tskId: Int
    ): Response<TaskDetailsResponse>


    @PUT("api/Teacher/UpdateTaskWithQuestions")
    suspend fun updateTaskWithQuestions(
        @Query("tsk_id") tskId: Int,
        @Body request: TaskWithQuestionsUpdateRequest
    ): Response<ApiResponse>


    @GET("api/Teacher/GetTaskResults")
    suspend fun getTaskResults(
        @Query("tsk_id") taskId: Int
    ): TaskResultsResponse


    // Update question marks
    @POST("api/Teacher/UpdateQuestionMarks")
    suspend fun updateQuestionMarks(
        @Body request: MarkUpdateRequest
    ): Response<ApiResponseFORUPDATEMARKS>


    @GET("api/Teacher/GetCourseResults")
    fun GetCourseResults(
        @Query("oc_id") ocId: Int
    ): Call<CourseResultsResponse>


    @GET("api/Teacher/GetMarksDistribution")
    suspend fun getMarksDistribution(@Query("oc_id") ocId: Int): Response<MarksDistributionResponse>

    @GET("api/Teacher/GetHomeworkByOcId")
    suspend fun getHomeworkByOcId(@Query("oc_id") ocId: Int): Response<HomeworkResponse>


    // New POST method for update
    @POST("api/Teacher/UpdateMarksDistribution")
    suspend fun updateMarksDistribution(@Body request: MarksDistributionRequest): Response<md_respone>


    @GET("api/Student/GetStudentResults")
    fun getStudentResults(@Query("s_id") studentId: String): Call<StudentAcademicRecord>


    // In your ApiService interface
    @GET("api/Teacher/GetQuestionCloMappingsByTaskIdSimple")
    suspend fun getQuestionCloMappingsByTaskId(@Query("tsk_id") taskId: Int): Response<CloMappingResponse>

    @POST("api/Teacher/UpdateQuestionCloMappingPercentage")
    suspend fun updateCloMappingPercentage(@Body request: UpdatePercentageRequest): Response<ApiResponseclomappingupdation>

    @GET("api/Teacher/GetAdvisorInfo")
    suspend fun GetAdvisorInfo(
        @Query("T_id") teacherId: String
    ): Response<AdvisorInfoResponse>


    @GET("api/Teacher/GetStudentsByProgramSemesterSection")
    suspend fun GetStudentsByProgramSemesterSection(
        @Query("p_id") p_id: Int,
        @Query("sem") sem: String,
        @Query("section") section: String
    ): Response<StudentsResponse>


    @GET("api/OBE/GetTeacherMappingsCountByProgram") // Adjust "YourController" to your actual controller name
    suspend fun getTeacherMappingsCountByProgram(@Query("programId") programId: Int): Response<CountResponse>


    // Add to your existing ApiService interface
    @GET("api/student/GetStudentPloResult")
    suspend fun getStudentPloResult(
        @Query("S_id") studentId: String
    ): Response<List<PloResultResponse>>


    //////////////////////////fcr//////////////////////////////


    // Get Offered Course Details
    @GET("api/Teacher/GetOfferedCourseDetails")
    suspend fun getOfferedCourseDetails(
        @Query("oc_id") ocId: Int
    ): Response<OfferedCourseDetailsResponse>

    // Get Program PLOs by Offered Course
    @GET("api/Teacher/GetProgramPlosByOfferedCourse")
    suspend fun getProgramPlosByOfferedCourse(
        @Query("oc_id") ocId: Int
    ): Response<List<PLOResponse>>

    // Get PLO Map CLO Records
    @GET("api/Teacher/GetPloMapCloRecords")
    suspend fun getPloMapCloRecords(
        @Query("oc_id") ocId: Int
    ): Response<List<PloMapCloResponse>>

    // Get Course Grade Distribution
    @GET("api/Teacher/GetCourseGradeDistribution")
    suspend fun getCourseGradeDistribution(
        @Query("oc_id") ocId: Int
    ): Response<GradeDistributionResponse>

    // Get All CLOs with Grades
    @GET("api/Teacher/GetAllCloWithGrades")
    suspend fun getAllCloWithGrades(
        @Query("oc_id") ocId: Int
    ): Response<List<CloGradeResponse>>


    @GET("api/Teacher/GetCLOs") // Replace with your actual endpoint
    suspend fun getCLOs(@Query("oc_id") ocId: Int): List<CLOTEST>




    @GET("api/Teacher/GetActivitiesbtn")
    suspend fun GetActivitiesbtn(
        @Query("oc_id") ocId: Int,
        @Query("T_id") teacherId: String
    ): Response<List<ActivityType>>



    @GET("api/Student/Getstudentploresult") // Replace with your actual endpoint
    suspend fun Getstudentploresults(@Query("S_id") studentId: String): List<PloResultResponse>





}











