package io.github.nahkd123.vmulti4j.descriptor;

import java.util.Optional;
import java.util.function.Predicate;

public interface ItemQueriable {
	Optional<Item> query(Predicate<Item> predicate);
}
