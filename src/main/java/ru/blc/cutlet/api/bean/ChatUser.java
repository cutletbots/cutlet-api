package ru.blc.cutlet.api.bean;

import ru.blc.cutlet.api.command.Messenger;

public record ChatUser(Messenger messenger, long userId) {

}
