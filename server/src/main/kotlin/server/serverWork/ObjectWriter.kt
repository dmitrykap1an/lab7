package server.serverWork

import general.AppIO.Answer
import java.io.ObjectOutputStream
import java.util.Objects
import java.util.concurrent.Callable

class ObjectWriter : Callable<Boolean> {

    private val outt : ObjectOutputStream;
    private val answer: Answer;

    constructor(outt : ObjectOutputStream, answer : Answer){
        this.outt = outt;
        this.answer = answer
    }

    override fun call(): Boolean {
        outt.writeObject(answer)
        outt.flush()
        return true
    }

}