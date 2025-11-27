# ITI Attendance V2.0

تطبيق ويب يعتمد على Spring Boot مع واجهات عربية مستوحاة من موقع معهد تكنولوجيا المعلومات (ITI) لإدارة بيانات الموارد البشرية والحضور.

## نظرة عامة على الهيكل
- **Java 17 / Spring Boot 3.2** مع Thymeleaf لتصميم واجهات عربية ملونة.
- **Spring Data JPA** للتعامل مع كيانات MySQL (إنشاء تلقائي للجداول عبر `ddl-auto=update`).
- **الأمان**: تم تمكين Spring Security مع إعداد مفتوح لتجربة الواجهة، ويمكن لاحقًا ربطه بمزوّد مصادقة حقيقي.
- **التخزين**: اتصال MySQL مع قاعدة بيانات `iti_attendance` (عدّل بيانات الدخول في `src/main/resources/application.properties`).

## الكيانات والخدمات
- `Organization` (المؤسسة) مع الفروع والأقسام التابعة.
- `Branch` (فرع) مرتبط بالمؤسسة ومدير فرع.
- `Department` (قسم) مرتبط بالفرع مع مدير قسم.
- `JobTitle` (مسمى وظيفي) يربط الموظفين بمسمى معتمد.
- `Employee` (موظف) مع حالتي اعتماد PENDING/ACTIVE ودعم أدوار الموارد البشرية/المديرين/الأمن، مع الحقول `nationalId` (14 خانة)، `branch`، `department`، `manager`، و`jobTitle`.
- `AttendanceRule` (قاعدة حضور) لكل مؤسسة/فرع/قسم مع مواعيد الدوام ودقائق السماح.
- `AttendanceRecord` (سجل حضور) يشمل ربط الفرع لمتابعة الفروع المتعددة.
- `LeaveType` (نوع إجازة) مع قواعد الحد الأقصى واشتراطات الموافقات.
- `ApprovalLevel` (سلسلة اعتماد) تحدد ترتيب الاعتماد حسب نوع الطلب والدور.
- `AccessPolicy` (سياسات الوصول) لتحديد الصفحات المتاحة لكل دور نظام.
- `LeaveRequest` (طلب إجازة) لتتبع فترات الإجازات والحالة.

كل خدمة (`*Service`) توفر:
- CRUD مع حذف ناعم.
- استيراد جماعي من ملفات Excel باستخدام قالب قابل للتنزيل.

## نقاط النهاية الرئيسية
كل نقطة نهاية لها مسار للإدارة `/admin/...` ومسار للموظفين `/employee/...`:
- المؤسسة: `/admin/organizations`, `/admin/organizations/new`, `/admin/organizations/import`, `/admin/organizations/template`.
- الفروع: `/admin/branches`, `/admin/branches/new`, `/admin/branches/import`, `/admin/branches/template`.
- الأقسام: `/admin/departments`, `/admin/departments/new`, `/admin/departments/import`, `/admin/departments/template`.
- المسميات الوظيفية: `/admin/job-titles`, `/admin/job-titles/new`, `/admin/job-titles/import`, `/admin/job-titles/template`.
- الموظفون: `/admin/employees` (قائمة)، `/admin/employees/new` (نموذج)، `/admin/employees/import`, `/admin/employees/template` مع ربط الفرع/القسم/المسمى/المدير.
- قواعد الحضور: `/admin/rules`, `/admin/rules/new`, `/admin/rules/import`, `/admin/rules/template`.
- سجلات الحضور: `/admin/attendance`, `/admin/attendance/new`, `/admin/attendance/import`, `/admin/attendance/template`.
- حد تنبيهات الحضور: `/admin/attendance-limit` لتحديد نسبة الحضور التي يعتمد عليها تنبيه المديرين لمرؤوسيهم.
- أنواع الإجازات: `/admin/leave-types`, `/admin/leave-types/new`, `/admin/leave-types/import`, `/admin/leave-types/template`.
- مستويات الاعتماد: `/admin/approvals`, `/admin/approvals/new`, `/admin/approvals/import`, `/admin/approvals/template`.
- سياسات الوصول: `/admin/access-policies`, `/admin/access-policies/new`, `/admin/access-policies/import`, `/admin/access-policies/template`.
- طلبات الإجازة: `/admin/leaves`, `/admin/leaves/new`, `/admin/leaves/import`, `/admin/leaves/template`.
- بوابة الموظف: `/employee/portal` لعرض السجلات والطلبات والرسائل القادمة من المدير.
- لوحة المدير: `/manager/dashboard` لاستعراض نسب حضور الفريق وإرسال تنبيهات للموظفين منخفضي الحضور.
- تسجيل المستخدم: `/register`، دخول عام للموظف/المدير: `/login`، ودخول الإدارة: `/admin/login` مع إعادة توجيه لحالة الانتظار `/admin/pending/notify`.

## الواجهات
- **الصفحة الرئيسية** (`/`) تحتوي على أزرار للانتقال بين قسم الإدارة وقسم الموظفين مع بطاقات توضح الخدمات.
- **قسم الإدارة**: يتضمن لوحة تحكم وروابط لكل خدمة مع استيراد جماعي وحذف ناعم.
- **قسم الموظفين**: يعرض سجلات الحضور وطلبات الإجازة والرسائل بعد الدخول.
- **قسم المدير**: يوضح نسب حضور الموظفين مقابل الحد المعتمد مع إمكانية إرسال تنبيه يظهر في بوابة الموظف.
- الواجهات جميعها بالعربية وبألوان مستوحاة من الهوية البصرية لـ ITI.

## التشغيل محليًا
1. تأكد من تشغيل MySQL وتعديل بيانات الاتصال في `application.properties`.
2. من جذر المشروع شغّل:
   ```bash
   mvn spring-boot:run
   ```
3. افتح المتصفح على `http://localhost:8080` لاستخدام الواجهة.

### تسجيل الدخول كمسؤول موارد بشرية لأول مرة
1. من صفحة التسجيل `/register` أضف حسابًا جديدًا واختر الدور `HR_MANAGER` أو `HR_EMPLOYEE` مع بريد وكلمة مرور معروفتين لديك.
2. فعّل الحساب من قاعدة بيانات MySQL بتعديل الحالة إلى `ACTIVE` (مثال: `UPDATE employee SET status='ACTIVE' WHERE email='example@iti.eg';`).
3. سجّل الدخول من `/login` بالبريد وكلمة المرور التي سجلت بها، ثم انتقل إلى صفحات التهيئة (`/admin/organizations`, `/admin/branches`, ...).
4. كلمات المرور يتم تشفيرها تلقائيًا عند التسجيل أو إضافة الموظف من لوحة الإدارة، ولا يمكن تسجيل الدخول إلا للحسابات النشطة ACTIVE عبر Spring Security.
5. بعد تسجيل الدخول لأول مرة، عيّن صلاحيات كل نوع مستخدم عبر صفحة سياسات الوصول `/admin/access-policies` حتى يتمكن باقي الأدوار من استعمال واجهاتهم.
6. حدّد نسبة الحضور الدنيا عبر `/admin/attendance-limit` لضمان ظهور تنبيهات انخفاض الحضور في لوحة المدير.

## ملاحظات الاستخدام
- الحذف يتم بشكل ناعم عبر حقل `deleted` للحفاظ على البيانات.
- الاستيراد الجماعي يتوقع ملفات Excel وفق القوالب المتاحة للتنزيل من كل صفحة إدارة.
- تسجيل الدخول الإداري يتحقق من البريد وكلمة المرور المخزّنين أثناء التسجيل؛ في حالة انتظار الاعتماد يتم توجيه المستخدم لصفحة إشعار الموارد البشرية.
- توجد صفحتا أخطاء: وضع التطوير يعرض التفاصيل الكاملة عند تفعيل الخاصية `app.error.mode=dev`، ووضع الإنتاج يعرض رسالة ودية عند ضبط القيمة إلى `prod`.

