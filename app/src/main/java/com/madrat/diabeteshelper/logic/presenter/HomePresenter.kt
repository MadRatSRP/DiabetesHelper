package com.madrat.diabeteshelper

import com.madrat.diabeteshelper.logic.Home


class HomePresenter(private val view: HomeMVP.View,
                    private val repository: HomeMVP.Repository)
    : HomeMVP.Presenter  {

    override fun setListOfHomes() {
        val listOfHomes = ArrayList<Home>()

        listOfHomes.add(
            Home(
                "Boris",
                "70.0"
            )
        )
        listOfHomes.add(
            Home(
                "Semen",
                "54"
            )
        )
        listOfHomes.add(
            Home(
                "Alexei",
                "72.4"
            )
        )
        listOfHomes.add(
            Home(
                "Gennadiy",
                "12"
            )
        )

        view.updateListOfHomes(listOfHomes)
    }

    /*override fun returnDisplayName(): String {
        val fullAccount = client?.users()?.currentAccount

        return fullAccount?.name?.displayName!!
    }

    override fun getDisplayNameDisposable(): @NonNull Disposable? {
        return Observable.fromCallable {
            returnDisplayName()
        }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe{result->
                view.showDisplayName(result)
            }
    }*/
}