package rip.ada.groups.report;

import java.util.List;

public record Report(String name, List<String> headers, List<List<String>> rows) {
}
