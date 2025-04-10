package com.oussama.masaratalnur.di

import com.oussama.masaratalnur.data.repository.AuthRepository
import com.oussama.masaratalnur.data.repository.AuthRepositoryImpl
import com.oussama.masaratalnur.data.repository.ContentRepository
import com.oussama.masaratalnur.data.repository.ContentRepositoryImpl
import com.oussama.masaratalnur.data.repository.UserRepository
import com.oussama.masaratalnur.data.repository.UserRepositoryImpl

object ServiceLocator {
    @Volatile private var userRepository: UserRepository? = null
    @Volatile private var authRepository: AuthRepository? = null // Add AuthRepository instance
    @Volatile private var contentRepository: ContentRepository? = null // Add ContentRepository instance

    fun provideUserRepository(): UserRepository {
        synchronized(this) {
            return userRepository ?: createUserRepository()
        }
    }

    fun provideAuthRepository(): AuthRepository { // Add provider function
        synchronized(this) {
            return authRepository ?: createAuthRepository()
        }
    }

    fun provideContentRepository(): ContentRepository { // Add provider
        synchronized(this) {
            return contentRepository ?: createContentRepository()
        }
    }

    
    private fun createUserRepository(): UserRepository {
        val newRepo = UserRepositoryImpl()
        userRepository = newRepo
        return newRepo
    }

    private fun createAuthRepository(): AuthRepository { // Add creator function
        val newRepo = AuthRepositoryImpl()
        authRepository = newRepo
        return newRepo
    }

    private fun createContentRepository(): ContentRepository { // Add creator
        val newRepo = ContentRepositoryImpl()
        contentRepository = newRepo
        return newRepo
    }
}