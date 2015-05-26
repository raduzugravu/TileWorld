import static reactor.event.selector.Selectors.*

includes = 'push'

doWithReactor = {
    reactor('browser') {

        ext 'browser', [
                R('drawTileWorld'),
                R('updateConsole'),
                R('endGame'),
                'system',
                'client'
        ]
    }

    reactor('grailsReactor'){
        ext 'browser', ['sleepBrowser']

        stream('sleepBrowser'){
            filter{
                println 'filtered'
                it.data == 'no'
            }
        }
    }
}