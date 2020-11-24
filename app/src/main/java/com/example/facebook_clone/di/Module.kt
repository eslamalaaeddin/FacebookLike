package com.example.facebook_clone.di

import com.example.facebook_clone.repository.PostsRepository
import com.example.facebook_clone.repository.UsersRepository
import com.example.facebook_clone.viewmodel.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module


val databaseModule = module {
    fun provideDB(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }
    single { provideDB() }
}

val firebaseAuthModule = module {
    fun provideAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }
    single { provideAuth() }
}

val firebaseStorageModule = module {
    fun provideStorage() : FirebaseStorage {
        return FirebaseStorage.getInstance()
    }
    single { provideStorage() }
}

//////////////////////////////////////////////////VIEW MODELS///////////////////////////////////////
val passFragViewModelModule = module { viewModel { PasswordFragmentViewModel(get(),get()) }}

val loginFragViewModelModule = module {viewModel { LoginFragmentViewModel(get(), get()) } }

val forgetPasswordFragViewModelModule = module { viewModel { ForgetPasswordFragmentViewModel(get()) } }

val profileActivityViewModelModule = module { viewModel { ProfileActivityViewModel(get()) } }

val profilePictureActivityViewModelModule = module { viewModel { ProfilePictureActivityViewModel(get()) } }

val postCreatorViewModelModule = module { viewModel { PostViewModel(get()) } }

val searchActivityViewModelModule = module { viewModel { SearchActivityViewModel(get()) } }

val othersProfileActivityViewModelModule = module { viewModel { OthersProfileActivityViewModel(get()) } }

val notificationsFragmentViewModelModule = module { viewModel { NotificationsFragmentViewModel(get()) } }

val newsFeedActivityViewModelModule = module { viewModel { NewsFeedActivityViewModel(get()) } }
val recentUsersActivityViewModelModule = module { viewModel { RecentUsersActivityViewModel(get()) } }

/////////////////////////////////////////////////REPOSITORIES///////////////////////////////////////

val usersRepositoryModule = module { single { UsersRepository(get(),get(), get()) } }
val postsRepositoryModule = module { single { PostsRepository(get(),get(), get()) } }