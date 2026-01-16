package com.smartbudget.app.data.remote;

import java.util.List;

public class GeminiModels {

    // Request
    public static class Request {
        public List<Content> contents;

        public Request(List<Content> contents) {
            this.contents = contents;
        }
    }

    public static class Content {
        public String role;
        public List<Part> parts;

        public Content(String role, String text) {
            this.role = role;
            this.parts = java.util.Collections.singletonList(new Part(text));
        }
        
        public Content(String role, List<Part> parts) {
            this.role = role;
            this.parts = parts;
        }
    }

    public static class Part {
        public String text;

        public Part(String text) {
            this.text = text;
        }
    }

    // Response
    public static class Response {
        public List<Candidate> candidates;
    }

    public static class Candidate {
        public Content content;
    }

    // Model List
    public static class ModelList {
        public List<Model> models;
    }

    public static class Model {
        public String name;
        public List<String> supportedGenerationMethods;
    }
}
