package com.example.todoapp

sealed class Screens(val route : String) {
    object Home : Screens("home_route")
    object Plan : Screens("plan_route")
}