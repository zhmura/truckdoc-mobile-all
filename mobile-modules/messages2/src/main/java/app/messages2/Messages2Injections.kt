package app.messages2

interface Messages2Injector {

    fun inject(act: ContactListActivity)
    fun inject(act: ChatActivity)
}

interface Messages2InjectorProvider {
    fun appComponent(): Messages2Injector
}
