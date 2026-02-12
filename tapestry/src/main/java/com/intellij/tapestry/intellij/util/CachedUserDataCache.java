package com.intellij.tapestry.intellij.util;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.psi.util.CachedValue;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.tapestry.intellij.TapestryModuleSupportLoader;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Alexey Chmutov
 * 
 * Updated for IntelliJ 2025.3 compatibility - no longer extends UserDataCache
 * due to workspace model changes where Module doesn't implement UserDataHolderEx
 */
public abstract class CachedUserDataCache<T, Owner> {
  private final String keyName;
  private final ConcurrentHashMap<Owner, CachedValue<T>> cache = new ConcurrentHashMap<>();

  public CachedUserDataCache(@NonNls String keyName) {
    this.keyName = keyName;
  }

  protected final CachedValue<T> compute(final Owner owner, Object p) {
    return CachedValuesManager.getManager(getProject(owner)).createCachedValue(
      () -> CachedValueProvider.Result.create(computeValue(owner), getDependencies(owner)), false);
  }

  @Nullable
  protected abstract T computeValue(Owner owner);

  protected Object[] getDependencies(Owner owner) {
    return new Object[]{owner};
  }

  protected abstract Project getProject(Owner projectOwner);

  public final T get(Owner owner) {
    // For Module owners, check if module is disposed
    if (owner instanceof Module) {
      Module module = (Module) owner;
      if (module.isDisposed()) {
        cache.remove(owner);
        return null;
      }
    }
    
    CachedValue<T> cachedValue = cache.computeIfAbsent(owner, o -> compute(o, null));
    return cachedValue.getValue();
  }
  
  public final void clear(Owner owner) {
    cache.remove(owner);
  }
  
  public final void clearAll() {
    cache.clear();
  }
}