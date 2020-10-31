package app.messages2

import android.app.Activity

interface Messages2Injector {

    fun inject(act: ContactListActivity)
    fun inject(f: ChatActivity)
/*
    fun chatFactory(): ChatComponent.Factory*/
}

/*@Subcomponent(modules = [ChatComponent.Module::class])
interface ChatComponent {

    @dagger.Module
     class Module {
        @Provides
        fun binds(c: ChatActivity): Activity = c
    }

    @Subcomponent.Factory
    interface Factory {
        fun factory(@BindsInstance f: Activity): ChatComponent
    }
}*/

//TODO can we do that via subcomponent?
interface MessageDependenciesProvider {
    fun provideOnMessageMenu(activity: Activity): OnMessagesMenu
    fun provideOnMessageClicked(activity: Activity): OnMessageClicked
}

interface Messages2InjectorProvider {
    fun appComponent(): Messages2Injector
}
