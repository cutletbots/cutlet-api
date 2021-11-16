package ru.blc.cutlet.api.bot;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class BotDescription {
    private File file;
    private String name, main;
    private String description, website, version;
    private List<String> depends = new ArrayList<>();
    private List<String> softDepends = new ArrayList<>();
    private String author = "";
    private List<String> modules = new ArrayList<>();

}
