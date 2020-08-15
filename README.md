### References
* [Статья, на основе которого будет писаться биндинги](https://habr.com/ru/company/mobileup/blog/342850/)
* [Explanation of RecyclerView](https://ziginsider.github.io/RecyclerView/)
* [ContourLayout](https://github.com/cashapp/contour)
* [SaveState ViewModel](https://developer.android.com/topic/libraries/architecture/viewmodel-savedstate)

### MINOR TODO
* Fix bottom sheet dialog on orientation changed (wrong width and height)
* VTextField
* Add guidlines and barriers to VConstraint
* Replace LinearLayout with LinearLayoutCompat
* VRecycler pagination
* SharedPreferences interfaces like Retrofit's services
* FIX RxPermissions
* Move throttleClick to arguments of onClick (???)
* FIX wrong casting density (see: LayoutSize)
* Make WidgetContainer interface generatable for fixing boilerplate functions like vLabel, vFrame etc.

### MAJOR TODO
* Identify first
* Implement toolbar badges
* Menu for BottomNavigationView
* Navigating to another fragment, on view with 'to' prop clicked
* Make drawable's dsl to set ripple or color effect reactive
* Light and dark themes for views
* VAutocomplete, VCheckbox, VRadio
* Get image and video from camera
* Get image and video from gallery
* Splash screen with permissions requirement
* Learn more about Exceptions
* Use AppCompat realizations of views instead (for example LinearLayoutCompat, AppCompatImageView etc.) (???)
* VPermission (see TedPermission)
* SecuredPreferences
* Research about [RxActivityResult](https://github.com/VictorAlbertos/RxActivityResult)
* Generate to standard architectures

### Plan to v0.1.0
* Rename project to Kotlandro
* Make each UiEntity to have only one and empty constructor
* Move layoutSize param inside prop instead of constructor
* Use sealed classes for styles instead of lambda
* Generate itself classes for each WidgetElement
* Use Either or something else for title or color. [Example](https://blog.usejournal.com/dysfunctional-programming-in-java-4-no-nulls-allowed-88e8735475a).

### DONE
* ~~Implement view sizes~~
* ~~Show and hide Dialogs~~
* ~~Show and hide BottomSheetDialog~~
* ~~Make relays lifecycle aware~~
* ~~VRecycler for one typed items~~
* ~~Implement ConstraintLayout's pattern of markup~~
* ~~Implement dialog (alert, bottomSheetView)~~
* ~~Implement recyclerview's several viewTypes~~
* ~~VRelative~~
* ~~VHorizontal and VVertical~~
* ~~VEdit~~
* ~~Generating itself classes for WidgetContainers~~
* ~~VButton (MaterialButton is still in alpha)~~
* ~~Loading button~~
* ~~Menu for DrawerLayout~~

### Thanks to
* [Vuetify library for inspiration](https://vuetifyjs.com)
* Code generation posts:
  - https://habr.com/ru/company/e-Legion/blog/413603/
  - https://square.github.io/kotlinpoet
  - https://medium.com/tompee/kotlin-annotation-processor-and-code-generation-58bd7d0d333b
* [Post about making progress drawable inside button](https://proandroiddev.com/replace-progressdialog-with-a-progress-button-in-your-app-14ed1d50b44)
* Permissions handling library:
  - https://github.com/tbruyelle/RxPermissions
  - https://github.com/vanniktech/RxPermission
  - https://github.com/ParkSangGwon/TedPermission
* [DrawableToolbox](https://github.com/duanhong169/DrawableToolbox) - lib helps to create drawables programmatically
* [Paris](https://github.com/airbnb/paris)
* [Using GSON with Kotlin’s Non-Null Types](https://medium.com/swlh/using-gson-with-kotlins-non-null-types-468b1c66bd8b)