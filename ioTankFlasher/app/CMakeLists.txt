# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.4.1)

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.

include_directories(src/main/cpp/ src/main/cpp/spiffs src/main/cpp/tclap)

add_library(spiffs_cache src/main/cpp/spiffs/spiffs_cache.c)
add_library(spiffs_check src/main/cpp/spiffs/spiffs_check.c)
add_library(spiffs_gc src/main/cpp/spiffs/spiffs_gc.c)
add_library(spiffs_hydrogen src/main/cpp/spiffs/spiffs_hydrogen.c)
add_library(spiffs_nucleus src/main/cpp/spiffs/spiffs_nucleus.c)


add_executable( # Sets the name of the library.
             main

             # Provides a relative path to your source file(s).
             src/main/cpp/main.cpp )


target_link_libraries(main  spiffs_check spiffs_hydrogen spiffs_nucleus spiffs_cache spiffs_gc)

set(EXECUTABLE_OUTPUT_PATH      "${CMAKE_CURRENT_SOURCE_DIR}/src/main/assets/${ANDROID_ABI}")
