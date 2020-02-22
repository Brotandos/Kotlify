### References
* [Статья, на основе которого будет писаться биндинги](https://habr.com/ru/company/mobileup/blog/342850/)
* [Explanation of RecyclerView](https://ziginsider.github.io/RecyclerView/)
* [ContourLayout](https://github.com/cashapp/contour)

### MINOR TODO
* Fix bottom sheet dialog on orientation changed (wrong width and height)
* VTextField
* Add guidlines and barriers to VConstraint
* Replace LinearLayout with LinearLayoutCompat
* Generate itself classes for WidgetElement realizations
* VRecycler pagination
* Use sealed classes for styles instead of lambda
* SharedPreferences interfaces like Retrofit's services
* FIX RxPermissions

### MAJOR TODO
* Rename project to Kotlandro
* Implement toolbar badges
* Menu for BottomNavigationView and DrawerLayout
* Implement menu for drawer and bottomNavView
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

### Thanks to
* [Vuetify library for inspiration](https://vuetifyjs.com)
* Code generation papers:
  - https://habr.com/ru/company/e-Legion/blog/413603/
  - https://square.github.io/kotlinpoet
  - https://medium.com/tompee/kotlin-annotation-processor-and-code-generation-58bd7d0d333b
