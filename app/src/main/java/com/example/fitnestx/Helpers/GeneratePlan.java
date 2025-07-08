package com.example.fitnestx.Helpers;

import com.example.fitnestx.data.entity.ExerciseEntity;
import com.example.fitnestx.data.entity.MuscleGroupEntity;
import com.example.fitnestx.data.entity.SessionExerciseEntity;
import com.example.fitnestx.data.entity.WorkoutPlanEntity;
import com.example.fitnestx.data.entity.WorkoutSessionEntity;
import com.example.fitnestx.data.repository.ExerciseRepository;
import com.example.fitnestx.data.repository.MuscleGroupRepository;
import com.example.fitnestx.data.repository.SessionExerciseRepository;
import com.example.fitnestx.data.repository.WorkoutPlanRepository;
import com.example.fitnestx.data.repository.WorkoutSessionRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class GeneratePlan {
    private int planId;
    private double bmi;
    private String goal;
    private boolean gender;

    private int daysPerWeek;
    private ExerciseRepository exerciseRepository;
    private MuscleGroupRepository muscleGroupRepository;
    private WorkoutSessionRepository workoutSessionRepository;
    private WorkoutSessionEntity workoutSessionEntity;
    private SessionExerciseRepository sessionExerciseRepository;
    private WorkoutPlanRepository workoutPlanRepository;

    public GeneratePlan(int planId, double bmi, String goal, boolean gender, int daysPerWeek, ExerciseRepository exerciseRepository, MuscleGroupRepository muscleGroupRepository, WorkoutSessionRepository workoutSessionRepository, SessionExerciseRepository sessionExerciseRepository, WorkoutPlanRepository workoutPlanRepository) {
        this.planId = planId;
        this.bmi = bmi;
        this.goal = goal;
        this.gender = gender;

        this.daysPerWeek = daysPerWeek;
        this.exerciseRepository = exerciseRepository;
        this.muscleGroupRepository = muscleGroupRepository;
        this.workoutSessionRepository = workoutSessionRepository;
        this.sessionExerciseRepository = sessionExerciseRepository;
        this.workoutPlanRepository = workoutPlanRepository;
    }

    public void Generation() {
        InsertWorkoutSession();
        InsertByBmiAndTargetAndGender();
    }

    public void InsertWorkoutSession() {
        for (int index = 1; index <= daysPerWeek; index++) {
            workoutSessionEntity = new WorkoutSessionEntity(planId, "Ngày " + index, 1, false);
            workoutSessionRepository.insertWorkoutSession(workoutSessionEntity);
        }
    }

    public void InsertByBmiAndTargetAndGender() {
        if ("improve_shape".equals(goal) && daysPerWeek== 2) {
            insertExerciseCaseSpecCase1Case4();
        } else if (("lean_tone".equals(goal) || "lose_fat".equals(goal)) && daysPerWeek == 2) {
            insertExerciseCaseSpecCase2Case3();
        }
        else if ("improve_shape".equals(goal) && bmi == 21.5 && gender) {
            insertExerciseCase1();
        } else if ("lean_tone".equals(goal) && bmi == 21.5  && gender) {
            insertExerciseCase2();
        } else if ("lose_fat".equals(goal) && bmi == 21.5  && gender) {
            insertExerciseCase2();
            WorkoutPlanEntity workoutPlanEntity = workoutPlanRepository.getWorkoutPlanById(planId);
            workoutPlanEntity.setNote("Chế độ ăn nên nạp nhiều protein, ít carb và hạn chế đồ dầu mỡ nhé");
            workoutPlanRepository.updateWorkoutPlan(workoutPlanEntity);
        } else if ("improve_shape".equals(goal) && !gender && bmi == 21.5)  {
            insertExerciseCase4();
        }
    }

    Map<Integer, List<ExerciseEntity>> groupExercisesByParent() {
        List<ExerciseEntity> exercises = exerciseRepository.getAllExercises();
        List<MuscleGroupEntity> allMuscles = muscleGroupRepository.getListMuscleGroup();
        Map<Integer, List<ExerciseEntity>> groupedByParent = new HashMap<>();
        Map<Integer, Integer> childToParentMap = new HashMap<>();

        for (MuscleGroupEntity mg : allMuscles) {
            if (mg.getParentId() != null) {
                childToParentMap.put(mg.getMuscleGroupId(), mg.getParentId());
            }
        }

        for (ExerciseEntity ex : exercises) {
            int childId = ex.getMuscleGroupId();
            Integer parentId = childToParentMap.get(childId);
            int key = (parentId != null) ? parentId : childId;

            groupedByParent.computeIfAbsent(key, k -> new ArrayList<>()).add(ex);
        }

        return groupedByParent;
    }

    List<ExerciseEntity> getRandomExercisesOfParent(int parentId, int count) {
        Map<Integer, List<ExerciseEntity>> grouped = groupExercisesByParent();
        List<ExerciseEntity> list = grouped.getOrDefault(parentId, new ArrayList<>());
        if (list.size() <= count) return new ArrayList<>(list);

        Collections.shuffle(list);
        return new ArrayList<>(list.subList(0, count));
    }
    public int getRandomNumberFromList (List < Integer > numbers) {
        if (numbers == null || numbers.isEmpty()) {
            throw new IllegalArgumentException("Danh sách rỗng!");
        }

        Random random = new Random();
        return numbers.get(random.nextInt(numbers.size()));
    }

        void insertExerciseCase1() {

        List<ExerciseEntity> pushExercises1 = getRandomExercisesOfParent(1, 2);
        List<ExerciseEntity> pushExercises2 = getRandomExercisesOfParent(2, 2);
        List<ExerciseEntity> pushExercises3 = getRandomExercisesOfParent(5, 2);
        List<ExerciseEntity> pushExercises4 = getRandomExercisesOfParent(1, 2);
        List<ExerciseEntity> pushExercises5 = getRandomExercisesOfParent(2, 2);
        List<ExerciseEntity> pushExercises6 = getRandomExercisesOfParent(5, 2);



        List<ExerciseEntity> pullExercises1 = getRandomExercisesOfParent(3, 3);
        List<ExerciseEntity> pullExercises2 = getRandomExercisesOfParent(4, 3);
        List<ExerciseEntity> pullExercises3 = getRandomExercisesOfParent(3, 3);
        List<ExerciseEntity> pullExercises4 = getRandomExercisesOfParent(4, 3);

        List<ExerciseEntity> legExercise1 = getRandomExercisesOfParent(6, 3);
        List<ExerciseEntity> legExercise2 = getRandomExercisesOfParent(7, 3);
        List<ExerciseEntity> legExercise3 = getRandomExercisesOfParent(6, 3);
        List<ExerciseEntity> legExercise4 = getRandomExercisesOfParent(7, 3);

        //upper
        List<Integer> optionsPush = Arrays.asList(1, 2, 5);
        List<Integer> optionsPull = Arrays.asList(3, 4);
        int randomNumberPush = getRandomNumberFromList(optionsPush);
        int randomNumberPull = getRandomNumberFromList(optionsPull);

        List<ExerciseEntity> upperExercises1 = getRandomExercisesOfParent(randomNumberPush, 3);
        List<ExerciseEntity> upperExercises2 = getRandomExercisesOfParent(randomNumberPull, 3);

        //FullBody
        List<Integer> optionsPushFullBody = Arrays.asList(1, 2, 5);
        List<Integer> optionsPullFullBody = Arrays.asList(3, 4);
        List<Integer> optionsLegFullBody = Arrays.asList(6, 7);
        int randomNumberPushFB = getRandomNumberFromList(optionsPushFullBody);
        int randomNumberPullFB = getRandomNumberFromList(optionsPullFullBody);
        int randomNumberLegFB = getRandomNumberFromList(optionsLegFullBody);
        List<ExerciseEntity> fullBodyExercises1 = getRandomExercisesOfParent(randomNumberPushFB, 2);
        List<ExerciseEntity> fullBodyExercises2 = getRandomExercisesOfParent(randomNumberPullFB, 2);
        List<ExerciseEntity> fullBodyExercises3 = getRandomExercisesOfParent(randomNumberLegFB, 2);


        List<WorkoutSessionEntity> workoutSessions = workoutSessionRepository.getWorkoutSessionsByPlanId(planId);
        for (WorkoutSessionEntity wS : workoutSessions) {
            if (wS.getDate().equals("Ngày 1")) {
                for (ExerciseEntity ex : pushExercises1) {
                    sessionExerciseRepository.insertSessionExercise(new SessionExerciseEntity(wS.getSessionId(), ex.getExerciseId(), 1, "60s", 3, 10, false, false));
                }
                for (ExerciseEntity ex : pushExercises2) {
                    sessionExerciseRepository.insertSessionExercise(new SessionExerciseEntity(wS.getSessionId(), ex.getExerciseId(), 1, "60s", 3, 10, false, false));
                }
                for (ExerciseEntity ex : pushExercises3) {
                    sessionExerciseRepository.insertSessionExercise(new SessionExerciseEntity(wS.getSessionId(), ex.getExerciseId(), 1, "60s", 3, 10, false, false));
                }

            } else if (wS.getDate().equals("Ngày 2")) {
                for (ExerciseEntity ex : pullExercises1) {
                    sessionExerciseRepository.insertSessionExercise(new SessionExerciseEntity(wS.getSessionId(), ex.getExerciseId(), 1, "60s", 3, 10, false, false));
                }
                for (ExerciseEntity ex : pullExercises2) {
                    sessionExerciseRepository.insertSessionExercise(new SessionExerciseEntity(wS.getSessionId(), ex.getExerciseId(), 1, "60s", 3, 10, false, false));
                }
            } else if (wS.getDate().equals("Ngày 3")) {
                for (ExerciseEntity ex : legExercise1) {
                    sessionExerciseRepository.insertSessionExercise(new SessionExerciseEntity(wS.getSessionId(), ex.getExerciseId(), 1, "60s", 3, 10, false, false));
                }
                for (ExerciseEntity ex : legExercise2) {
                    sessionExerciseRepository.insertSessionExercise(new SessionExerciseEntity(wS.getSessionId(), ex.getExerciseId(), 1, "60s", 3, 10, false, false));
                }
            } else if ("Ngày 4".equals(wS.getDate()) && daysPerWeek == 6) {
                for (ExerciseEntity ex : pushExercises4) {
                    sessionExerciseRepository.insertSessionExercise(new SessionExerciseEntity(wS.getSessionId(), ex.getExerciseId(), 1, "60s", 3, 10, false, false));
                }
                for (ExerciseEntity ex : pushExercises5) {
                    sessionExerciseRepository.insertSessionExercise(new SessionExerciseEntity(wS.getSessionId(), ex.getExerciseId(), 1, "60s", 3, 10, false, false));
                }
                for (ExerciseEntity ex : pushExercises6) {
                    sessionExerciseRepository.insertSessionExercise(new SessionExerciseEntity(wS.getSessionId(), ex.getExerciseId(), 1, "60s", 3, 10, false, false));
                }
            } else if ("Ngày 4".equals(wS.getDate()) && daysPerWeek == 5) {
                for (ExerciseEntity ex : upperExercises1) {
                    sessionExerciseRepository.insertSessionExercise(new SessionExerciseEntity(wS.getSessionId(), ex.getExerciseId(), 1, "60s", 3, 10, false, false));
                }
                for (ExerciseEntity ex : upperExercises2) {
                    sessionExerciseRepository.insertSessionExercise(new SessionExerciseEntity(wS.getSessionId(), ex.getExerciseId(), 1, "60s", 3, 10, false, false));
                }
            } else if ("Ngày 4".equals(wS.getDate()) && daysPerWeek == 4) {
                for (ExerciseEntity ex : fullBodyExercises1) {
                    sessionExerciseRepository.insertSessionExercise(new SessionExerciseEntity(wS.getSessionId(), ex.getExerciseId(), 1, "60s", 3, 10, false, false));
                }
                for (ExerciseEntity ex : fullBodyExercises2) {
                    sessionExerciseRepository.insertSessionExercise(new SessionExerciseEntity(wS.getSessionId(), ex.getExerciseId(), 1, "60s", 3, 10, false, false));
                }
                for (ExerciseEntity ex : fullBodyExercises3) {
                    sessionExerciseRepository.insertSessionExercise(new SessionExerciseEntity(wS.getSessionId(), ex.getExerciseId(), 1, "60s", 3, 10, false, false));
                }
            } else if ("Ngày 5".equals(wS.getDate()) && daysPerWeek == 6) {
                for (ExerciseEntity ex : pullExercises3) {
                    sessionExerciseRepository.insertSessionExercise(new SessionExerciseEntity(wS.getSessionId(), ex.getExerciseId(), 1, "60s", 3, 10, false, false));
                }
                for (ExerciseEntity ex : pullExercises4) {
                    sessionExerciseRepository.insertSessionExercise(new SessionExerciseEntity(wS.getSessionId(), ex.getExerciseId(), 1, "60s", 3, 10, false, false));
                }
            } else if ("Ngày 5".equals(wS.getDate()) && daysPerWeek == 5) {
                for (ExerciseEntity ex : legExercise3) {
                    sessionExerciseRepository.insertSessionExercise(new SessionExerciseEntity(wS.getSessionId(), ex.getExerciseId(), 1, "60s", 3, 10, false, false));
                }
                for (ExerciseEntity ex : legExercise4) {
                    sessionExerciseRepository.insertSessionExercise(new SessionExerciseEntity(wS.getSessionId(), ex.getExerciseId(), 1, "60s", 3, 10, false, false));
                }

            } else if ("Ngày 6".equals(wS.getDate()) && daysPerWeek == 6) {
                for (ExerciseEntity ex : legExercise3) {
                    sessionExerciseRepository.insertSessionExercise(new SessionExerciseEntity(wS.getSessionId(), ex.getExerciseId(), 1, "60s", 3, 10, false, false));
                }
                for (ExerciseEntity ex : legExercise4) {
                    sessionExerciseRepository.insertSessionExercise(new SessionExerciseEntity(wS.getSessionId(), ex.getExerciseId(), 1, "60s", 3, 10, false, false));
                }
            }
        }
    }



        void insertExerciseCase2 () {

            List<ExerciseEntity> pushExercises1 = getRandomExercisesOfParent(1, 2);
            List<ExerciseEntity> pushExercises2 = getRandomExercisesOfParent(2, 2);
            List<ExerciseEntity> pushExercises3 = getRandomExercisesOfParent(5, 2);
            List<ExerciseEntity> pushExercises7;
            List<ExerciseEntity> pushExercises8 ;
            List<ExerciseEntity> pushExercises9;

            List<ExerciseEntity> pullExercises1 = getRandomExercisesOfParent(3, 3);
            List<ExerciseEntity> pullExercises2 = getRandomExercisesOfParent(4, 3);
            List<ExerciseEntity> pullExercises3 = getRandomExercisesOfParent(3, 2);
            List<ExerciseEntity> pullExercises4 = getRandomExercisesOfParent(4, 2);

            List<ExerciseEntity> legExercise1 = getRandomExercisesOfParent(6, 3);
            List<ExerciseEntity> legExercise2 = getRandomExercisesOfParent(7, 3);
            List<ExerciseEntity> legExercise3 = getRandomExercisesOfParent(6, 2);
            List<ExerciseEntity> legExercise4 = getRandomExercisesOfParent(7, 2);

            // 5 bài + 1 cardio
            List<ExerciseEntity> cardioExercisesRd = getRandomExercisesOfParent(29, 1);
            List<ExerciseEntity> cardioExercisesRd2 = getRandomExercisesOfParent(29, 1);
            List<ExerciseEntity> cardioExercisesRd3 = getRandomExercisesOfParent(29, 1);

            List<Integer> optionsPushRd = Arrays.asList(1, 2, 5);
            int randomNumberPushRd = getRandomNumberFromList(optionsPushRd);
             pushExercises7 = getRandomExercisesOfParent(randomNumberPushRd, 1);
            if (randomNumberPushRd == 1){
                 pushExercises8 = getRandomExercisesOfParent(2, 2);
                 pushExercises9 = getRandomExercisesOfParent(5, 2);

            } else if (randomNumberPushRd == 2) {
                pushExercises8 = getRandomExercisesOfParent(1, 2);
                pushExercises9 = getRandomExercisesOfParent(5, 2);

            }else{
                pushExercises8 = getRandomExercisesOfParent(1, 2);
                pushExercises9 = getRandomExercisesOfParent(2, 2);
            }
            //cardio
            List<ExerciseEntity> cardioExercises = getRandomExercisesOfParent(29, 6);


            //FullBody
            List<Integer> optionsPushFullBody = Arrays.asList(1, 2, 5);
            List<Integer> optionsPullFullBody = Arrays.asList(3, 4);
            List<Integer> optionsLegFullBody = Arrays.asList(6, 7);
            int randomNumberPushFB = getRandomNumberFromList(optionsPushFullBody);
            int randomNumberPullFB = getRandomNumberFromList(optionsPullFullBody);
            int randomNumberLegFB = getRandomNumberFromList(optionsLegFullBody);
            List<ExerciseEntity> fullBodyExercises1 = getRandomExercisesOfParent(randomNumberPushFB, 2);
            List<ExerciseEntity> fullBodyExercises2 = getRandomExercisesOfParent(randomNumberPullFB, 2);
            List<ExerciseEntity> fullBodyExercises3 = getRandomExercisesOfParent(randomNumberLegFB, 2);


            List<WorkoutSessionEntity> workoutSessions = workoutSessionRepository.getWorkoutSessionsByPlanId(planId);
            for (WorkoutSessionEntity wS : workoutSessions) {
                if (wS.getDate().equals("Ngày 1")) {
                    for (ExerciseEntity ex : pushExercises1) {
                        sessionExerciseRepository.insertSessionExercise(new SessionExerciseEntity(wS.getSessionId(), ex.getExerciseId(), 1, "60s", 3, 10, false, false));
                    }
                    for (ExerciseEntity ex : pushExercises2) {
                        sessionExerciseRepository.insertSessionExercise(new SessionExerciseEntity(wS.getSessionId(), ex.getExerciseId(), 1, "60s", 3, 10, false, false));
                    }
                    for (ExerciseEntity ex : pushExercises3) {
                        sessionExerciseRepository.insertSessionExercise(new SessionExerciseEntity(wS.getSessionId(), ex.getExerciseId(), 1, "60s", 3, 10, false, false));
                    }

                } else if ("Ngày 1".equals(wS.getDate())&& daysPerWeek == 3) {
                    for (ExerciseEntity ex : pushExercises7) {
                        sessionExerciseRepository.insertSessionExercise(new SessionExerciseEntity(wS.getSessionId(), ex.getExerciseId(), 1, "60s", 3, 10, false, false));
                    }
                    for (ExerciseEntity ex : pushExercises8) {
                        sessionExerciseRepository.insertSessionExercise(new SessionExerciseEntity(wS.getSessionId(), ex.getExerciseId(), 1, "60s", 3, 10, false, false));
                    }
                    for (ExerciseEntity ex : pushExercises9) {
                        sessionExerciseRepository.insertSessionExercise(new SessionExerciseEntity(wS.getSessionId(), ex.getExerciseId(), 1, "60s", 3, 10, false, false));
                    }
                    for (ExerciseEntity ex : cardioExercisesRd) {
                        sessionExerciseRepository.insertSessionExercise(new SessionExerciseEntity(wS.getSessionId(), ex.getExerciseId(), 1, "60s", 3, 10, false, false));
                    }
                } else if (wS.getDate().equals("Ngày 2")) {
                    for (ExerciseEntity ex : pullExercises1) {
                        sessionExerciseRepository.insertSessionExercise(new SessionExerciseEntity(wS.getSessionId(), ex.getExerciseId(), 1, "60s", 3, 10, false, false));
                    }
                    for (ExerciseEntity ex : pullExercises2) {
                        sessionExerciseRepository.insertSessionExercise(new SessionExerciseEntity(wS.getSessionId(), ex.getExerciseId(), 1, "60s", 3, 10, false, false));
                    }
                } else if ("Ngày 2".equals(wS.getDate())&& daysPerWeek == 3) {
                    for (ExerciseEntity ex : pullExercises3) {
                        sessionExerciseRepository.insertSessionExercise(new SessionExerciseEntity(wS.getSessionId(), ex.getExerciseId(), 1, "60s", 3, 10, false, false));
                    }
                    for (ExerciseEntity ex : pullExercises4) {
                        sessionExerciseRepository.insertSessionExercise(new SessionExerciseEntity(wS.getSessionId(), ex.getExerciseId(), 1, "60s", 3, 10, false, false));
                    }
                    for (ExerciseEntity ex : cardioExercisesRd2) {
                        sessionExerciseRepository.insertSessionExercise(new SessionExerciseEntity(wS.getSessionId(), ex.getExerciseId(), 1, "60s", 3, 10, false, false));
                    }
                } else if (wS.getDate().equals("Ngày 3")) {
                    for (ExerciseEntity ex : legExercise1) {
                        sessionExerciseRepository.insertSessionExercise(new SessionExerciseEntity(wS.getSessionId(), ex.getExerciseId(), 1, "60s", 3, 10, false, false));
                    }
                    for (ExerciseEntity ex : legExercise2) {
                        sessionExerciseRepository.insertSessionExercise(new SessionExerciseEntity(wS.getSessionId(), ex.getExerciseId(), 1, "60s", 3, 10, false, false));
                    }

                } else if ("Ngày 3".equals(wS.getDate()) && daysPerWeek == 3){
                    for (ExerciseEntity ex : legExercise3) {
                        sessionExerciseRepository.insertSessionExercise(new SessionExerciseEntity(wS.getSessionId(), ex.getExerciseId(), 1, "60s", 3, 10, false, false));
                    }
                    for (ExerciseEntity ex : legExercise4) {
                        sessionExerciseRepository.insertSessionExercise(new SessionExerciseEntity(wS.getSessionId(), ex.getExerciseId(), 1, "60s", 3, 10, false, false));
                    }
                    for (ExerciseEntity ex : cardioExercisesRd3) {
                        sessionExerciseRepository.insertSessionExercise(new SessionExerciseEntity(wS.getSessionId(), ex.getExerciseId(), 1, "60s", 3, 10, false, false));
                    }
                } else if ("Ngày 4".equals(wS.getDate()) && daysPerWeek == 6) {
                    for (ExerciseEntity ex : cardioExercises) {
                        sessionExerciseRepository.insertSessionExercise(new SessionExerciseEntity(wS.getSessionId(), ex.getExerciseId(), 1, "60s", 3, 10, false, false));
                    }

                } else if ("Ngày 4".equals(wS.getDate()) && daysPerWeek == 5) {
                    for (ExerciseEntity ex : fullBodyExercises1) {
                        sessionExerciseRepository.insertSessionExercise(new SessionExerciseEntity(wS.getSessionId(), ex.getExerciseId(), 1, "60s", 3, 10, false, false));
                    }
                    for (ExerciseEntity ex : fullBodyExercises2) {
                        sessionExerciseRepository.insertSessionExercise(new SessionExerciseEntity(wS.getSessionId(), ex.getExerciseId(), 1, "60s", 3, 10, false, false));
                    }
                    for (ExerciseEntity ex : fullBodyExercises3) {
                        sessionExerciseRepository.insertSessionExercise(new SessionExerciseEntity(wS.getSessionId(), ex.getExerciseId(), 1, "60s", 3, 10, false, false));
                    }

                } else if ("Ngày 4".equals(wS.getDate()) && daysPerWeek == 4) {
                    for (ExerciseEntity ex : cardioExercises) {
                        sessionExerciseRepository.insertSessionExercise(new SessionExerciseEntity(wS.getSessionId(), ex.getExerciseId(), 1, "60s", 3, 10, false, false));
                    }
                } else if ("Ngày 5".equals(wS.getDate()) && daysPerWeek == 6) {
                    for (ExerciseEntity ex : fullBodyExercises1) {
                        sessionExerciseRepository.insertSessionExercise(new SessionExerciseEntity(wS.getSessionId(), ex.getExerciseId(), 1, "60s", 3, 10, false, false));
                    }
                    for (ExerciseEntity ex : fullBodyExercises2) {
                        sessionExerciseRepository.insertSessionExercise(new SessionExerciseEntity(wS.getSessionId(), ex.getExerciseId(), 1, "60s", 3, 10, false, false));
                    }
                    for (ExerciseEntity ex : fullBodyExercises3) {
                        sessionExerciseRepository.insertSessionExercise(new SessionExerciseEntity(wS.getSessionId(), ex.getExerciseId(), 1, "60s", 3, 10, false, false));
                    }
                }  else if ("Ngày 5".equals(wS.getDate()) && daysPerWeek == 5) {
                    for (ExerciseEntity ex : cardioExercises) {
                        sessionExerciseRepository.insertSessionExercise(new SessionExerciseEntity(wS.getSessionId(), ex.getExerciseId(), 1, "60s", 3, 10, false, false));
                    }
                }
                else if ("Ngày 6".equals(wS.getDate()) && daysPerWeek == 6) {
                    for (ExerciseEntity ex : cardioExercises) {
                        sessionExerciseRepository.insertSessionExercise(new SessionExerciseEntity(wS.getSessionId(), ex.getExerciseId(), 1, "60s", 3, 10, false, false));
                    }
                }
            }
        }



        void insertExerciseCase4 () {

        List<ExerciseEntity> pushExercises1 = getRandomExercisesOfParent(1, 2);
        List<ExerciseEntity> pushExercises2 = getRandomExercisesOfParent(2, 2);
        List<ExerciseEntity> pushExercises3 = getRandomExercisesOfParent(5, 2);
      

        List<ExerciseEntity> pullExercises1 = getRandomExercisesOfParent(3, 3);
        List<ExerciseEntity> pullExercises2 = getRandomExercisesOfParent(4, 3);
        
        List<ExerciseEntity> legExercise1 = getRandomExercisesOfParent(6, 3);
        List<ExerciseEntity> legExercise2 = getRandomExercisesOfParent(7, 3);
        List<ExerciseEntity> legExercise3 = getRandomExercisesOfParent(6, 3);
        List<ExerciseEntity> legExercise4 = getRandomExercisesOfParent(7, 3);

        
        //cardio
        List<ExerciseEntity> cardioExercises = getRandomExercisesOfParent(29, 6);
        
        
        List<WorkoutSessionEntity> workoutSessions = workoutSessionRepository.getWorkoutSessionsByPlanId(planId);
        for (WorkoutSessionEntity wS : workoutSessions) {
            if (wS.getDate().equals("Ngày 1")) {
                for (ExerciseEntity ex : legExercise1) {
                    sessionExerciseRepository.insertSessionExercise(new SessionExerciseEntity(wS.getSessionId(), ex.getExerciseId(), 1, "60s", 3, 10, false, false));
                }
                for (ExerciseEntity ex : legExercise2) {
                    sessionExerciseRepository.insertSessionExercise(new SessionExerciseEntity(wS.getSessionId(), ex.getExerciseId(), 1, "60s", 3, 10, false, false));
                }


            }  else if (wS.getDate().equals("Ngày 2")) {
                for (ExerciseEntity ex : pullExercises1) {
                    sessionExerciseRepository.insertSessionExercise(new SessionExerciseEntity(wS.getSessionId(), ex.getExerciseId(), 1, "60s", 3, 10, false, false));
                }
                for (ExerciseEntity ex : pullExercises2) {
                    sessionExerciseRepository.insertSessionExercise(new SessionExerciseEntity(wS.getSessionId(), ex.getExerciseId(), 1, "60s", 3, 10, false, false));
                }
            } else if (wS.getDate().equals("Ngày 3")) {
                for (ExerciseEntity ex : pushExercises1) {
                    sessionExerciseRepository.insertSessionExercise(new SessionExerciseEntity(wS.getSessionId(), ex.getExerciseId(), 1, "60s", 3, 10, false, false));
                }
                for (ExerciseEntity ex : pushExercises2) {
                    sessionExerciseRepository.insertSessionExercise(new SessionExerciseEntity(wS.getSessionId(), ex.getExerciseId(), 1, "60s", 3, 10, false, false));
                }
                for (ExerciseEntity ex : pushExercises3) {
                    sessionExerciseRepository.insertSessionExercise(new SessionExerciseEntity(wS.getSessionId(), ex.getExerciseId(), 1, "60s", 3, 10, false, false));
                }

            }else if ("Ngày 4".equals(wS.getDate())) {
                for (ExerciseEntity ex : cardioExercises) {
                    sessionExerciseRepository.insertSessionExercise(new SessionExerciseEntity(wS.getSessionId(), ex.getExerciseId(), 1, "60s", 3, 10, false, false));
                }

            }
             else if ("Ngày 5".equals(wS.getDate())) {
                for (ExerciseEntity ex : legExercise3) {
                    sessionExerciseRepository.insertSessionExercise(new SessionExerciseEntity(wS.getSessionId(), ex.getExerciseId(), 1, "60s", 3, 10, false, false));
                } 
                for (ExerciseEntity ex : legExercise4) {
                    sessionExerciseRepository.insertSessionExercise(new SessionExerciseEntity(wS.getSessionId(), ex.getExerciseId(), 1, "60s", 3, 10, false, false));
                }
            }

        }
    }

        void insertExerciseCaseSpecCase1Case4 () {
            //upper
            List<Integer> optionsPush = Arrays.asList(1, 2, 5);
            List<Integer> optionsPull = Arrays.asList(3, 4);
            int randomNumberPush = getRandomNumberFromList(optionsPush);
            int randomNumberPull = getRandomNumberFromList(optionsPull);

            List<ExerciseEntity> upperExercises1 = getRandomExercisesOfParent(randomNumberPush, 3);
            List<ExerciseEntity> upperExercises2 = getRandomExercisesOfParent(randomNumberPull, 3);

            List<ExerciseEntity> legExercise1 = getRandomExercisesOfParent(6, 3);
            List<ExerciseEntity> legExercise2 = getRandomExercisesOfParent(7, 3);

            List<WorkoutSessionEntity> workoutSessions = workoutSessionRepository.getWorkoutSessionsByPlanId(planId);
            for (WorkoutSessionEntity wS : workoutSessions) {
                if (wS.getDate().equals("Ngày 1")) {
                    for (ExerciseEntity ex : upperExercises1) {
                        sessionExerciseRepository.insertSessionExercise(new SessionExerciseEntity(wS.getSessionId(), ex.getExerciseId(), 1, "60s", 3, 10, false, false));
                    }
                    for (ExerciseEntity ex : upperExercises2) {
                        sessionExerciseRepository.insertSessionExercise(new SessionExerciseEntity(wS.getSessionId(), ex.getExerciseId(), 1, "60s", 3, 10, false, false));
                    }
                } else if ("Ngày 2".equals(wS.getDate())) {
                    for (ExerciseEntity ex : legExercise1) {
                        sessionExerciseRepository.insertSessionExercise(new SessionExerciseEntity(wS.getSessionId(), ex.getExerciseId(), 1, "60s", 3, 10, false, false));
                    }
                    for (ExerciseEntity ex : legExercise2) {
                        sessionExerciseRepository.insertSessionExercise(new SessionExerciseEntity(wS.getSessionId(), ex.getExerciseId(), 1, "60s", 3, 10, false, false));
                    }
                }
            }
        }

        void insertExerciseCaseSpecCase2Case3 () {
        //upper
        List<Integer> optionsPush = Arrays.asList(1, 2, 5);
        List<Integer> optionsPull = Arrays.asList(3, 4);
        int randomNumberPush = getRandomNumberFromList(optionsPush);
        int randomNumberPull = getRandomNumberFromList(optionsPull);
            List<ExerciseEntity> upperExercises1;
            List<ExerciseEntity> upperExercises2;
        int rd = getRandomNumberFromList(new ArrayList<>(Arrays.asList(randomNumberPush,randomNumberPull)));
        if(rd == randomNumberPush){
             upperExercises1 = getRandomExercisesOfParent(randomNumberPush, 2);
             upperExercises2 = getRandomExercisesOfParent(randomNumberPull, 3);

        }else{
             upperExercises1 = getRandomExercisesOfParent(randomNumberPull, 3);
             upperExercises2 = getRandomExercisesOfParent(randomNumberPush, 2);
        }


            List<Integer> optionsLeg = Arrays.asList(6, 7);
            List<ExerciseEntity> legExercise1;
            List<ExerciseEntity> legExercise2;
            int randomNumberLeg = getRandomNumberFromList(optionsLeg);
            if (randomNumberLeg == 6){
                 legExercise1 = getRandomExercisesOfParent(6, 2);
                 legExercise2 = getRandomExercisesOfParent(7, 3);

            }else{
                 legExercise1 = getRandomExercisesOfParent(6, 3);
                 legExercise2 = getRandomExercisesOfParent(7, 2);
            }



        List<ExerciseEntity> cardioExercisesRd = getRandomExercisesOfParent(29, 1);
        List<ExerciseEntity> cardioExercisesRd2 = getRandomExercisesOfParent(29, 1);



        List<WorkoutSessionEntity> workoutSessions = workoutSessionRepository.getWorkoutSessionsByPlanId(planId);
        for (WorkoutSessionEntity wS : workoutSessions) {
            if (wS.getDate().equals("Ngày 1")) {
                for (ExerciseEntity ex : upperExercises1) {
                    sessionExerciseRepository.insertSessionExercise(new SessionExerciseEntity(wS.getSessionId(), ex.getExerciseId(), 1, "60s", 3, 10, false, false));
                }
                for (ExerciseEntity ex : upperExercises2) {
                    sessionExerciseRepository.insertSessionExercise(new SessionExerciseEntity(wS.getSessionId(), ex.getExerciseId(), 1, "60s", 3, 10, false, false));
                }
                for (ExerciseEntity ex : cardioExercisesRd) {
                    sessionExerciseRepository.insertSessionExercise(new SessionExerciseEntity(wS.getSessionId(), ex.getExerciseId(), 1, "60s", 3, 10, false, false));
                }
            } else if ("Ngày 2".equals(wS.getDate())) {
                for (ExerciseEntity ex : legExercise1) {
                    sessionExerciseRepository.insertSessionExercise(new SessionExerciseEntity(wS.getSessionId(), ex.getExerciseId(), 1, "60s", 3, 10, false, false));
                }
                for (ExerciseEntity ex : legExercise2) {
                    sessionExerciseRepository.insertSessionExercise(new SessionExerciseEntity(wS.getSessionId(), ex.getExerciseId(), 1, "60s", 3, 10, false, false));
                }
                for (ExerciseEntity ex : cardioExercisesRd2) {
                    sessionExerciseRepository.insertSessionExercise(new SessionExerciseEntity(wS.getSessionId(), ex.getExerciseId(), 1, "60s", 3, 10, false, false));
                }
            }
        }
    }

    }


