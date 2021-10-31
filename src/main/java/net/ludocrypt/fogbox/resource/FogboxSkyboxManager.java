package net.ludocrypt.fogbox.resource;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import net.ludocrypt.fogbox.Fogbox;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.SinglePreparationResourceReloader;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;

public class FogboxSkyboxManager extends SinglePreparationResourceReloader<HashMap<Identifier, Identifier>> {
	public final HashMap<Identifier, Identifier> fogboxs = Maps.newHashMap();

	@Override
	protected HashMap<Identifier, Identifier> prepare(ResourceManager manager, Profiler profiler) {
		profiler.startTick();
		fogboxs.clear();
		for (Iterator<String> var4 = manager.getAllNamespaces().iterator(); var4.hasNext(); profiler.pop()) {
			String string = var4.next();
			profiler.push(string);
			try {
				List<Resource> list = manager.getAllResources(new Identifier(string, "fogboxes.json"));
				for (Iterator<Resource> var7 = list.iterator(); var7.hasNext(); profiler.pop()) {
					Resource resource = var7.next();
					profiler.push(resource.getResourcePackName());
					try {
						InputStream inputStream = resource.getInputStream();
						try {
							InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
							try {
								profiler.push("parse");
								JsonElement jsonElement = new JsonParser().parse(reader);
								jsonElement.getAsJsonObject().entrySet().forEach((pair) -> {
									String key = pair.getKey();
									Identifier id;
									if (key.contains(":")) {
										id = new Identifier(key);
									} else {
										id = new Identifier(string, key);
									}
									Identifier skyboxId = CodecUtil.get(Identifier.CODEC, pair.getValue());
									if (skyboxId != null) {
										add(id, skyboxId);
									}
								});

								profiler.pop();
							} catch (Throwable var16) {
								try {
									reader.close();
								} catch (Throwable var15) {
									var16.addSuppressed(var15);
								}
								throw var16;
							}
							reader.close();
						} catch (Throwable throwable) {
							if (inputStream != null) {
								try {
									inputStream.close();
								} catch (Throwable throwable2) {
									throwable.addSuppressed(throwable2);
								}
							}
							throw throwable;
						}
						if (inputStream != null) {
							inputStream.close();
						}
					} catch (RuntimeException invalid) {
						Fogbox.LOGGER.warn("Invalid fogboxes.json in resourcepack: '{}'", resource.getResourcePackName(), invalid);
					}
				}
			} catch (IOException io) {
			}
		}
		profiler.endTick();
		return this.fogboxs;
	}

	@Override
	protected void apply(HashMap<Identifier, Identifier> prepared, ResourceManager manager, Profiler profiler) {
		HashMap<Identifier, Identifier> map = Maps.newHashMap(prepared);
		this.fogboxs.clear();
		this.fogboxs.putAll(map);
	}

	protected void add(Identifier id, Identifier panGroup) {
		if (!fogboxs.containsKey(id)) {
			fogboxs.put(id, panGroup);
		} else {
			fogboxs.remove(id);
			fogboxs.put(id, panGroup);
		}
	}

}
