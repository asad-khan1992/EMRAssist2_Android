package com.emrassist.audio.di

import com.emrassist.audio.service.audiouploading.repository.UploadServiceRepository
import com.emrassist.audio.service.audiouploading.viewmodel.UploadServiceViewModel
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class ViewModelModules {
    @Singleton
    @Provides
    public fun getUploadServiceViewModel(repository: UploadServiceRepository): UploadServiceViewModel {
        return UploadServiceViewModel(repository)
    }
//    @get:Singleton
//    @get:Provides
//    val draweActivityViewModel: DrawerActivityViewModel
//        get() = DrawerActivityViewModel()
//
//    @get:Singleton
//    @get:Provides
//    val mainActivityViewModel: MainActivityViewModel
//        get() = MainActivityViewModel()
//
//    @get:Singleton
//    @get:Provides
//    val homeFragmentViewModel: HomeViewModel
//        get() = HomeViewModel()
//
//    @Provides
//    @Singleton
//    fun getBlogViewModel(apiManager: ApiManager?): BlogViewModel {
//        return BlogViewModel(apiManager)
//    }
//
//    @get:Singleton
//    @get:Provides
//    val settingsViewModel: SettingsViewModel
//        get() = SettingsViewModel()
//
//    @get:Singleton
//    @get:Provides
//    val main1ViewModel: Main1ViewModel
//        get() = Main1ViewModel()
//
//    @get:Singleton
//    @get:Provides
//    val main2ViewModel: Main2ViewModel
//        get() = Main2ViewModel()
//
//    @get:Singleton
//    @get:Provides
//    val slideShowViewModel: SlideshowViewModel
//        get() = SlideshowViewModel()
//
//    @get:Singleton
//    @get:Provides
//    val galleryViewModel: GalleryViewModel
//        get() = GalleryViewModel()
}