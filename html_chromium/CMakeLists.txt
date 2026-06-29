cmake_minimum_required(VERSION 3.10)
project(gmote-chromium CXX)

set(CMAKE_CXX_STANDARD 17)

add_library(html_chromium SHARED main.cpp)

target_link_libraries(html_chromium PRIVATE
    log
    android
    jnigraphics
)
