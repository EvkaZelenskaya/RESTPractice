package com.myrest.restpractice.controller;

import com.myrest.restpractice.excs.InvalidRequestParametersException;
import com.myrest.restpractice.excs.MicrochipNotFoundException;
import com.myrest.restpractice.model.Microchip;
import com.myrest.restpractice.service.GsonServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;


@RestController
@CrossOrigin
@RequestMapping("/api")
public class MainController {

    @Autowired
    GsonServiceImpl gsonService;

    @GetMapping("/{id}")
    public ResponseEntity<Microchip> getById(@PathVariable long id) {
        List<Microchip> microchipList = gsonService.getListFromJson();
        Microchip microchip = getMcFromListById(microchipList, id);
        return getOkStatus(microchip);
    }

    @GetMapping("/")
    public ResponseEntity<List<Microchip>> getAll(
            @RequestParam(name = "sortBy", required = false) String sortField
    ) {
        List<Microchip> microchipList = gsonService.getListFromJson();
        microchipList = switch (sortField.toLowerCase()) {
            case "id" -> microchipList
                    .stream()
                    .sorted(Comparator.comparingLong(Microchip::getId))
                    .toList();
            case "frametype" -> microchipList
                    .stream()
                    .sorted(Comparator.comparing(Microchip::getFrameType))
                    .toList();
            case "name" -> microchipList
                    .stream()
                    .sorted(Comparator.comparing(Microchip::getName))
                    .toList();
            case "price" -> microchipList
                    .stream()
                    .sorted(Comparator.comparingInt(Microchip::getPrice))
                    .toList();
            default -> throw new InvalidRequestParametersException(sortField);
        };
        return getOkStatus(microchipList);
    }

    @GetMapping("/volt")
    public ResponseEntity<Long> getAllByVoltage(
            @RequestParam(name = "volt", required = false, defaultValue = "5.0") double voltage
    ) {
        List<Microchip> microchipList = gsonService.getListFromJson();
        long mcWithVoltageAmount = microchipList
                .stream()
                .filter(microchip -> microchip.getVoltage() >= voltage)
                .count();
        if (mcWithVoltageAmount == 0) {
            return getNoContentStatusForNumber();
        }
        return getOkStatus(mcWithVoltageAmount);
    }

    @PostMapping("/")
    public ResponseEntity<List<Microchip>> createNewByList(
            @RequestBody List<Microchip> microchipList) {
        List<Microchip> microchipListCurr = gsonService.getListFromJson();
        microchipListCurr.addAll(microchipList);
        gsonService.putListToJson(microchipListCurr);
        return getCreatedStatus(microchipListCurr);
    }

    @PutMapping("/")
    public ResponseEntity<List<Microchip>> replaceFrameType(
            @RequestParam(name = "formerFrameType") String formerFrameType,
            @RequestParam(name = "newFrameType") String newFrameType,
            @RequestParam(name = "printOnlyReplaced", required = false, defaultValue = "true") Boolean printOnlyReplaced) {
        List<Microchip> fullMicrochipList = gsonService.getListFromJson();
        List<Microchip> replacedMicrochipList = fullMicrochipList
                .stream()
                .filter(microchip -> microchip.getFrameType().equals(formerFrameType))
                .peek(microchip -> microchip.setFrameType(newFrameType))
                .toList();
        if (replacedMicrochipList.isEmpty()) {
            return getNoContentStatus();
        }
        if (printOnlyReplaced) {
            System.out.println("List of replaced microchips: ");
            replacedMicrochipList
                    .forEach(System.out::println);
        }
        fullMicrochipList = fullMicrochipList
                .stream()
                .peek(microchip -> {
                    if (microchip.getFrameType().equals(formerFrameType)) {
                        microchip.setFrameType(newFrameType);
                    }
                })
                .toList();
        if (!printOnlyReplaced) {
            System.out.println("List of all microchips: ");
            fullMicrochipList
                    .forEach(System.out::println);
        }
        gsonService.putListToJson(fullMicrochipList);
        if (printOnlyReplaced) {
            return getOkStatus(replacedMicrochipList);
        } else {
            return getOkStatus(fullMicrochipList);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteById(@PathVariable long id) {
        List<Microchip> microchipList = gsonService.getListFromJson();
        Microchip microchip = getMcFromListById(microchipList, id);
        microchipList.remove(microchip);
        gsonService.putListToJson(microchipList);
        return getNoContentStatus();
    }

    private Microchip getMcFromListById(List<Microchip> microchipList, long id) {
        return microchipList
                .stream()
                .filter(microchipItem -> microchipItem.getId() == id)
                .findFirst()
                .orElseThrow(() -> new MicrochipNotFoundException(id));
    }

    private ResponseEntity<List<Microchip>> getNoContentStatus() {
        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .build();
    }

    private ResponseEntity<Long> getNoContentStatusForNumber() {
        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .build();
    }

    private ResponseEntity<List<Microchip>> getOkStatus(List<Microchip> microchipList) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(microchipList);
    }

    private ResponseEntity<List<Microchip>> getCreatedStatus(List<Microchip> microchipList) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(microchipList);
    }

    private ResponseEntity<Microchip> getOkStatus(Microchip microchip) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(microchip);
    }

    private ResponseEntity<Long> getOkStatus(long amount) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(amount);
    }
}

