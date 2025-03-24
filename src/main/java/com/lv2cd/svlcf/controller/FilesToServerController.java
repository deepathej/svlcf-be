package com.lv2cd.svlcf.controller;

import static com.lv2cd.svlcf.util.Constants.*;

import com.lv2cd.svlcf.config.SvlcfConfig;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import lombok.AllArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@SuppressWarnings("unused")
public class FilesToServerController {

  private SvlcfConfig svlcfConfig;

  /**
   * API is used to retrieve the file from the local path and serves it on the server
   *
   * @param dir where the file is saved
   * @param filename with extension
   * @return resource based on the file and dir name
   * @throws IOException thrown when unable to load file
   */
  @GetMapping(FILES_ENDPOINT)
  public ResponseEntity<Resource> getFile(@PathVariable String dir, @PathVariable String filename)
      throws IOException {
    File file = new File(svlcfConfig.getPdfOutputRootPath() + dir + SLASH + filename);
    if (file.exists()) {
      return ResponseEntity.ok()
          .contentType(MediaType.parseMediaType(Files.probeContentType(file.toPath())))
          .body(new UrlResource(file.toURI()));
    } else {
      return ResponseEntity.notFound().build();
    }
  }
}
