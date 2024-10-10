package com.example.todoapp.Database

class TokenRepository(private val tokenDAO: TokenDAO) {
    val token = tokenDAO.getToken()
    suspend fun insert(tokenEntity: TokenEntity){
        tokenDAO.insertToken(tokenEntity)
    }
    suspend fun update(tokenEntity: TokenEntity){
        tokenDAO.updateToken(tokenEntity)
    }
    suspend fun delete(){
        tokenDAO.deleteAll()
    }
}