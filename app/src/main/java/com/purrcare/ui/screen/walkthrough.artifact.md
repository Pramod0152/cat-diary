# Compilation Fixes for OutlinedTextField and ViewModel

I have fixed the compilation errors related to `OutlinedTextField` usage and a type mismatch in the `CatProfileViewModel`.

## Changes Made

### UI Screens
#### [DailyLogScreen.kt](file:///C:/Learn/workspace/app/src/main/java/com/purrcare/ui/screen/DailyLogScreen.kt)
- Fixed `OutlinedTextField` by passing `KeyboardOptions` instead of `KeyboardType` to the `keyboardOptions` parameter.
- Added missing import for `androidx.compose.foundation.text.KeyboardOptions`.

#### [ProfileScreen.kt](file:///C:/Learn/workspace/app/src/main/java/com/purrcare/ui/screen/ProfileScreen.kt)
- Fixed `OutlinedTextField` usage similarly to `DailyLogScreen.kt`.
- Added `@OptIn(ExperimentalMaterial3Api::class)` to support `TopAppBar`.
- Refactored `saveState` to use `collectAsState()` for proper state observation in Compose.

### ViewModel
#### [CatProfileViewModel.kt](file:///C:/Learn/workspace/app/src/main/java/com/purrcare/ui/viewmodel/CatProfileViewModel.kt)
- Fixed `selectedCat` type mismatch. It now correctly maps the `selectedCatId` (Long) to a `CatProfile` flow using `flatMapLatest`.
- Added necessary coroutine flow imports.

## Verification Results

### Automated Tests
- Ran `./gradlew :app:compileDebugKotlin` which now completes successfully.

```
{
  "status": "Build finished successfully."
}
```
