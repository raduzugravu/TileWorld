import org.grails.plugins.events.reactor.api.EventsApi
import reactor.core.configuration.DispatcherType

/**
 * Configure Reactor to deal with events.
 * When choosing THREAD_POOL_EXECUTOR for the dispatcher type, a pool of threads (38 by default) will be used to process all generated events.
 */
doWithReactor = {
    environment {

        //define default dispatcher id
        defaultDispatcher = "grailsDispatcher"

        //define a ThreadPoolExecutor Dispatcher identified by 'grailsDispatcher'
        dispatcher('grailsDispatcher') {
            type = DispatcherType.THREAD_POOL_EXECUTOR
        }

//        //define a ThreadPoolExecutor Dispatcher identified by 'customDispatcher'
//        dispatcher('customDispatcher') {
//            type = DispatcherType.THREAD_POOL_EXECUTOR
//        }

    }

    reactor(EventsApi.GRAILS_REACTOR) {

    }
}