package com.hostel.service;

import com.hostel.model.Hostel;
import java.util.List;

public interface HostelService {
    Hostel createHostel(String name, String address, String city, String managerId);

    boolean updateHostel(Hostel hostel);

    boolean deleteHostel(String hostelId);

    Hostel getHostelById(String hostelId);

    List<Hostel> getHostelsByManager(String managerId);

    List<Hostel> searchHostels(String city, String query);

    boolean suspendHostel(String hostelId);

    boolean activateHostel(String hostelId);
}
