# CZ3004-MDP-Algo-Simulator
---
Intellij Installation Guide
1. Pull the project from Github top menu item VCS > Get from Version Control.
2. After importing, go to the "Add Configuration" or "Edit Configuration" if you have already imported the project before and have tried running it.
3. Point the Main class to the main.Main java class. 
4. Enter this --module-path /path/to/javafx/sdk --add-modules javafx.controls,javafx.fxml string with the path to your javafx SDK installed earlier in step 2 into the VM options input field and click Ok.
5. Run the build.gradle file to download gradle dependencies.
6. Run the project from the Main.java file or from the configuration you have just created.

---
Full Installation Guide
1. Install JavaFX for your relevant OS from https://gluonhq.com/products/javafx/
2. Unzip the files into your folder of choice.
3. Using Intellij, import the project from this GitHub repository.
4. After importing, go to the "Add Configuration" or "Edit Configuration" if you have already imported the project before and have tried running it.
5. Point the Main class to the main.Main java class. 
6. Enter this --module-path /path/to/javafx/sdk --add-modules javafx.controls,javafx.fxml string with the path to your javafx SDK installed earlier in step 2 into the VM options input field and click Ok.
7. Run the project from the Main.java file or from the configuration you have just created.

---
## Known Configuration Errors
1. Gradle Verifier exception.
Solution: Click on File > Invalidate Caches / Restart from the top left hand corner menu item.

2. Java SDK not found
Solution: Press Ctrl + Alt + Shift + S to open project structure then check that under the SDKs section there is a valid JDK library that is being pointed to.

3. Gradle Dependencies not found.
Solution: Run the build.gradle file by right click on it and selecting the run option (Ctrl + Shift + F10).

---
## Side note
If the steps above do not work for you or throw some sort of error. Check out this link: https://www.jetbrains.com/help/idea/javafx.html

Note:
- So far the project has been tested and works on JDK 11 and 14.
